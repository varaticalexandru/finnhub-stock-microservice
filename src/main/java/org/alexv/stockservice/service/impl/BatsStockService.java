package org.alexv.stockservice.service.impl;

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
import org.alexv.stockservice.model.Stock;
import org.alexv.stockservice.service.StockService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.alexv.stockservice.utils.Utilities.zip;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatsStockService implements StockService {

    private final FinnhubClient finnhubClient;
    private final Mapper<EnrichedSymbol, Stock> stockMapper;
    private final Mapper<Quote, StockPriceDto> stockPriceMapper;


    @Async
    public CompletableFuture<List<EnrichedSymbol>> getStockByTickerAsync(String ticker) {
        return finnhubClient.searchStock("US", "BATS", ticker);
    }

    @Async
    public CompletableFuture<Quote> getStockPriceByTickerAsync(String ticker) {
        return finnhubClient.getQuote(ticker);
    }

    @Override
    public Stock getStockByTicker(String ticker) {
        log.info("Getting stock from Finnhub.");
        var cf = getStockByTickerAsync(ticker);
        var resultList = cf.join();

        if (resultList.isEmpty()) {
            log.error("No stock found by ticker: {}", ticker);
            throw new StockNotFoundException(String.format("Stock %S not found.", ticker));
        }

        var item = resultList.getFirst();

        return stockMapper.mapTo(item);
    }

    @Override
    public StocksDto getStocksByTickers(TickersDto tickers) {
        log.info("Getting stocks from Finnhub.");
        List<CompletableFuture<List<EnrichedSymbol>>> cfList = new ArrayList<>();

        tickers
                .getTickers()
                .forEach(ticker -> cfList.add(getStockByTickerAsync(ticker)));

       var result = zip(cfList.stream(), tickers.getTickers()
               .stream())
               .map(pair -> {
                   var symbolList = pair.getLeft().join();
                   var ticker = pair.getRight();

                    if (symbolList.isEmpty()) {
                        log.error("No stock found by ticker: {}.", ticker);
                        throw new StockNotFoundException(String.format("Stock %S not found.", ticker));
                    }
                    else
                        return symbolList.getFirst();
               })
               .map(stockMapper::mapTo)
               .toList();

        return new StocksDto(result);
    }

    @Override
    public StockPriceDto getStockPrice(String ticker) {
        log.info("Getting stock price from Finnhub.");
        var cf = getStockPriceByTickerAsync(ticker);
        var result = cf.join();

        if (result.getCurrentPrice() == 0) {
            log.error("No stock found by ticker: {}", ticker);
            throw new StockNotFoundException(String.format("Stock %S not found.", ticker));
        }

        var stockPrice = stockPriceMapper.mapTo(result);
        stockPrice.setTicker(ticker);
        return stockPrice;
    }

    @Override
    public StocksPricesDto getStocksPrices(TickersDto tickers) {
        log.info("Getting stocks prices from Finnhub.");
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
                        log.error("No stock found by ticker: {}.", ticker);
                        throw new StockNotFoundException(String.format("Stock %S not found.", ticker));
                    }

                })
                .toList();

        return new StocksPricesDto(stocksPrices);
    }
}
