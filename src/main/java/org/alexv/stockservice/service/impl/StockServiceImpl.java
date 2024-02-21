package org.alexv.stockservice.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexv.finnhubclient.client.FinnhubClient;
import org.alexv.finnhubclient.model.EnrichedSymbol;
import org.alexv.finnhubclient.model.Quote;
import org.alexv.stockservice.dto.StockPriceDto;
import org.alexv.stockservice.dto.StocksDto;
import org.alexv.stockservice.dto.StocksPricesDto;
import org.alexv.stockservice.dto.TickersDto;
import org.alexv.stockservice.exception.StockNotFoundException;
import org.alexv.stockservice.mapper.Mapper;
import org.alexv.stockservice.model.MIC;
import org.alexv.stockservice.model.Stock;
import org.alexv.stockservice.service.StockService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.alexv.stockservice.utils.Utilities.zip;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {

    private final FinnhubClient finnhubApi;
    private final Mapper<EnrichedSymbol, Stock> stockMapper;
    private final Mapper<Quote, StockPriceDto> stockPriceMapper;

    @Value("${finnhub.stocks.exchange:US}")
    private String exchange;
    private final static List<String> mics = MIC.getAll();

    @Async
    @Retry(name = "stock-retry")
    @CircuitBreaker(name = "stock-breaker")
    public CompletableFuture<List<EnrichedSymbol>> getStockByTickerAsync(String ticker) {
        return finnhubApi.searchAllStock(exchange, ticker);
    }

    @Async
    @Retry(name = "stock-retry")
    @CircuitBreaker(name = "stock-breaker")
    public CompletableFuture<List<EnrichedSymbol>> getStockByTickersAsync(List<String> tickers) {
        return finnhubApi.searchAllStock(exchange, mics, tickers);
    }


    @Async
    @Retry(name = "stock-retry")
    @CircuitBreaker(name = "stock-breaker")
    public CompletableFuture<Quote> getStockPriceByTickerAsync(String ticker) {
        return finnhubApi.getQuote(ticker);
    }


    @Override
    public Stock getStockByTicker(String ticker) {
        log.info("Getting stock from Finnhub: {}.", ticker);

        var cf = getStockByTickerAsync(ticker);

        var result = cf.join();

        if (result.isEmpty()) {
            log.error("No stock found by ticker: {}", ticker);
            throw new StockNotFoundException(String.format("Stock %S not found.", ticker));
        }

        var item = result.getFirst();

        return stockMapper.mapTo(item);
    }


    @Override
    public StocksDto getStocksByTickers(TickersDto tickers) {
        log.info("Getting stocks from Finnhub: {}.", tickers.getTickers());
        List<String> tickerList = tickers.getTickers();

        var cf = getStockByTickersAsync(tickers.getTickers());
        var result = cf.join();

        if (result.isEmpty()) {
            log.error("No stock found by tickers: {}", tickers);
            throw new StockNotFoundException(String.format("Stocks not found: %S.", tickers.getTickers()));
        }

        var stocks = result.stream()
                .map(stockMapper::mapTo)
                .toList();


        return new StocksDto(stocks);
    }

    @Override
    public StockPriceDto getStockPrice(String ticker) {
        log.info("Getting stock price from Finnhub: {}.", ticker);
        var cf = getStockPriceByTickerAsync(ticker);
        var result = cf.join();

        if (result.getCurrentPrice() == 0) {
            log.error("No stock found by ticker: {}", ticker);
            throw new StockNotFoundException(String.format("Stock not found: %S.", ticker));
        }

        var stockPrice = stockPriceMapper.mapTo(result);
        stockPrice.setTicker(ticker);
        return stockPrice;
    }

    @Override
    public StocksPricesDto getStocksPrices(TickersDto tickers) {
        log.info("Getting stocks prices from Finnhub: {}.", tickers.getTickers());
        List<CompletableFuture<Quote>> cfList = new ArrayList<>();

        tickers.getTickers()
                .forEach(ticker -> cfList.add(getStockPriceByTickerAsync(ticker)));


        var stocksPrices = zip(cfList.stream(), tickers.getTickers()
                .stream())
                .map(pair -> {
                    var quote = pair.getLeft().join();
                    var ticker = pair.getRight();

                    if (quote.getCurrentPrice() != 0) {
                        StockPriceDto stockPriceDto = stockPriceMapper.mapTo(quote);
                        stockPriceDto.setTicker(ticker);

                        return stockPriceDto;
                    } else {
                        /*log.error("No stock found by ticker: {}.", ticker);
                        throw new StockNotFoundException(String.format("Stock not found: %S.", ticker));*/
                        return StockPriceDto.builder().currentPrice(0d).build();
                    }

                })
                .filter(stockPriceDto -> stockPriceDto.getCurrentPrice() != 0)
                .toList();

        if (stocksPrices.isEmpty()) {
            log.error("No stock found by tickers: {}", tickers.getTickers());
            throw new StockNotFoundException(String.format("Stocks not found: %S.", tickers.getTickers()));
        }

        return new StocksPricesDto(stocksPrices);
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
}
