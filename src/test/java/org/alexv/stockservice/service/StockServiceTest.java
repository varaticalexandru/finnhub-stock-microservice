package org.alexv.stockservice.service;

import org.alexv.finnhubclient.client.FinnhubClient;
import org.alexv.finnhubclient.model.EnrichedSymbol;
import org.alexv.finnhubclient.model.Quote;
import org.alexv.stockservice.dto.StockPriceDto;
import org.alexv.stockservice.mapper.Mapper;
import org.alexv.stockservice.mapper.impl.StockMapper;
import org.alexv.stockservice.mapper.impl.StockPriceMapper;
import org.alexv.stockservice.model.Currency;
import org.alexv.stockservice.model.Stock;
import org.alexv.stockservice.service.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {

    @Mock
    private FinnhubClient finnhubClient;
    @Mock
    private CompletableFuture<List<EnrichedSymbol>> completableFuture;

    private StockServiceImpl stockService;

    private static final String TICKER = "test_ticker";
    private static final String NAME = "test_name";
    private static final String TYPE = "Etf";
    private static final Currency CURRENCY = Currency.USD;

    @BeforeEach
    void beforeEach() {
        List<EnrichedSymbol> list = new ArrayList<>();
        EnrichedSymbol enrichedSymbol = new EnrichedSymbol();
        enrichedSymbol.setDescription(NAME);
        enrichedSymbol.setCurrency(CURRENCY.currency());
        enrichedSymbol.setSymbol(TICKER);
        enrichedSymbol.setType(TYPE);
        list.add(enrichedSymbol);

        when(finnhubClient.searchAllStock(anyString(), anyList(), anyList()))
                .thenReturn(completableFuture);
        when(completableFuture.join())
                .thenReturn(list);

        ModelMapper modelMapper = new ModelMapper();
        Mapper<EnrichedSymbol, Stock> stockMapper = new StockMapper(modelMapper);
        Mapper<Quote, StockPriceDto> stockPriceMapper = new StockPriceMapper(modelMapper);

        stockService = new StockServiceImpl(finnhubClient, stockMapper, stockPriceMapper);
    }

    @Test
    void getStockByTicker() {
        Stock actualStock = stockService.getStockByTicker("test_ticker");
        assertEquals(actualStock.getName(), NAME);
        assertEquals(actualStock.getTicker(), TICKER);
        assertEquals(actualStock.getType(), TYPE);
        assertEquals(actualStock.getCurrency(), CURRENCY);

        verify(finnhubClient, times(1)).searchAllStock(anyString(), anyList(), anyList());
    }

}
