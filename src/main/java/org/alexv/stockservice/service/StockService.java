package org.alexv.stockservice.service;

import org.alexv.stockservice.dto.StockPriceDto;
import org.alexv.stockservice.dto.StocksDto;
import org.alexv.stockservice.dto.StocksPricesDto;
import org.alexv.stockservice.dto.TickersDto;
import org.alexv.stockservice.model.Stock;

public interface StockService {

    Stock getStockByTicker(String ticker);

    StocksDto getStocksByTickers(TickersDto tickers);

    StockPriceDto getStockPrice(String ticker);

    StocksPricesDto getStocksPrices(TickersDto tickers);
}
