package org.alexv.stockservice.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {

    private final FinnhubClient finnhubApi;
    private final Mapper<EnrichedSymbol, Stock> stockMapper;
    private final Mapper<Quote, StockPriceDto> stockPriceMapper;

    @Value("${finnhub.stocks.exchange}")
    private String exchange;

    @Value("${finnhub.mics}")
    private List<String> mics;

    @Async
    @Retry(name = "stock-retry")
    @CircuitBreaker(name = "stock-breaker")
    public CompletableFuture<List<EnrichedSymbol>> getStocksByMicAsync(String mic) {
        return finnhubApi.searchStock(exchange, mic);
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

        List<CompletableFuture<List<EnrichedSymbol>>> completableFutures = new ArrayList<>();

        mics.forEach(mic -> completableFutures.add(getStocksByMicAsync(mic)));

        Optional<EnrichedSymbol> stock = completableFutures.stream()
                .map(CompletableFuture::join)
                .flatMap(Collection::stream)
                .filter(enrichedSymbol -> enrichedSymbol.getSymbol().equals(ticker))
                .findFirst()
                .or(() -> {
                    log.error("No stock found by ticker: {}", ticker);
                    throw new StockNotFoundException(String.format("Stock %S not found.", ticker));
                });

        return stockMapper.mapTo(stock.get());
    }


    @Override
    public StocksDto getStocksByTickers(TickersDto tickers) {

        List<String> tickersList = tickers.getTickers();
        log.info("Getting stocks from Finnhub: {}.", tickersList);

        List<CompletableFuture<List<EnrichedSymbol>>> completableFutures = new ArrayList<>();

        mics.forEach(mic -> completableFutures.add(getStocksByMicAsync(mic)));

        List<EnrichedSymbol> stocks = completableFutures.stream()
                .map(CompletableFuture::join)
                .flatMap(Collection::stream)
                .filter(enrichedSymbol -> tickersList.contains(enrichedSymbol.getSymbol()))
                .toList();

        List<String> stocksTickers = stocks
                .stream()
                .map(EnrichedSymbol::getSymbol)
                .toList();

        tickersList.removeAll(stocksTickers);

        if (!tickersList.isEmpty()) {
            log.error("No stocks found by tickers: {}", tickersList);
            throw new StockNotFoundException(String.format("Stocks not found: %S.", tickersList));
        }

        return new StocksDto(stockMapper.mapTo(stocks));
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

        List<String> tickersList = tickers.getTickers();
        log.info("Getting stocks prices from Finnhub: {}.", tickersList);
        List<CompletableFuture<Quote>> cfList = new ArrayList<>();

        tickersList.forEach(ticker -> cfList.add(getStockPriceByTickerAsync(ticker)));

        List<String> nonExistentTickers = new ArrayList<>();
        AtomicInteger tickerIdx = new AtomicInteger();

        List<StockPriceDto> stocksPrices = cfList
                .stream()
                .map(CompletableFuture::join)
                .map(quote -> {
                    if (quote.getCurrentPrice() == 0) {
                        nonExistentTickers.add(tickersList.get(tickerIdx.get()));
                    }

                    StockPriceDto stockPrice = stockPriceMapper.mapTo(quote);
                    stockPrice.setTicker(tickersList.get(tickerIdx.get()));
                    tickerIdx.incrementAndGet();
                    return stockPrice;
                })
                .filter(stockPriceDto -> stockPriceDto.getCurrentPrice() != 0)
                .toList();


        if (!nonExistentTickers.isEmpty()) {
            log.error("No stock found by tickers: {}", nonExistentTickers);
            throw new StockNotFoundException(String.format("Stocks not found: %S.", nonExistentTickers));
        }

        return new StocksPricesDto(stocksPrices);
    }

}
