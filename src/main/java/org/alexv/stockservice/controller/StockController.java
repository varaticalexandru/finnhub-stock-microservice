package org.alexv.stockservice.controller;

import lombok.RequiredArgsConstructor;
import org.alexv.stockservice.dto.StockPriceDto;
import org.alexv.stockservice.dto.StocksDto;
import org.alexv.stockservice.dto.StocksPricesDto;
import org.alexv.stockservice.dto.TickersDto;
import org.alexv.stockservice.model.Stock;
import org.alexv.stockservice.service.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stocks")
public class StockController {

    private final StockService stockService;

    @GetMapping("/{ticker}")
    public ResponseEntity<Stock> getStock(@PathVariable String ticker) {

        Stock stock = stockService.getStockByTicker(ticker);

        return new ResponseEntity<>(stock, HttpStatus.OK);
    }

    @PostMapping("/byTickers")
    public ResponseEntity<StocksDto> getStocksByTickers(@RequestBody TickersDto tickers) {
        var stocks = stockService.getStocksByTickers(tickers);

        return new ResponseEntity<>(stocks, HttpStatus.OK);
    }

    @GetMapping("/price/{ticker}")
    public ResponseEntity<StockPriceDto> getStockPrice(@PathVariable String ticker) {
        var stockPrice = stockService.getStockPrice(ticker);

        return new ResponseEntity<>(stockPrice, HttpStatus.OK);
    }

    @PostMapping("/price/byTickers")
    public ResponseEntity<StocksPricesDto> getStocksPrices(@RequestBody TickersDto tickers) {
        var stocksPrices = stockService.getStocksPrices(tickers);

        return new ResponseEntity<>(stocksPrices, HttpStatus.OK);
    }
}
