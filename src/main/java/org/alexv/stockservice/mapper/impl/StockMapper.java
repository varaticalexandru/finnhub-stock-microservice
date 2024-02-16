package org.alexv.stockservice.mapper.impl;

import org.alexv.finnhubclient.model.EnrichedSymbol;
import org.alexv.stockservice.mapper.Mapper;
import org.alexv.stockservice.model.Stock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockMapper implements Mapper<EnrichedSymbol, Stock> {

    private final ModelMapper modelMapper;

    @Autowired
    public StockMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;

        modelMapper.createTypeMap(EnrichedSymbol.class, Stock.class)
                .addMapping(EnrichedSymbol::getDescription, Stock::setName)
                .addMapping(EnrichedSymbol::getSymbol, Stock::setTicker)
                .addMapping(EnrichedSymbol::getCurrency, Stock::setCurrency)
                .addMapping(EnrichedSymbol::getMic, Stock::setSource)
                .addMapping(EnrichedSymbol::getType, Stock::setType)
                .addMapping(EnrichedSymbol::getCurrency, Stock::setCurrency);

        modelMapper.createTypeMap(Stock.class, EnrichedSymbol.class)
                .addMapping(Stock::getName, EnrichedSymbol::setDisplaySymbol)
                .addMapping(Stock::getTicker, EnrichedSymbol::setSymbol)
                .addMapping(Stock::getCurrency, EnrichedSymbol::setCurrency)
                .addMapping(Stock::getSource, EnrichedSymbol::setMic)
                .addMapping(Stock::getType, EnrichedSymbol::setType)
                .addMapping(Stock::getCurrency, EnrichedSymbol::setCurrency);
    }

    @Override
    public Stock mapTo(EnrichedSymbol symbol) {
        return modelMapper.map(symbol, Stock.class);
    }

    @Override
    public EnrichedSymbol mapFrom(Stock stock) {
        return modelMapper.map(stock, EnrichedSymbol.class);
    }

    @Override
    public List<Stock> mapTo(List<EnrichedSymbol> a) {
        return a.stream()
                .map(this::mapTo)
                .toList();
    }

    @Override
    public List<EnrichedSymbol> mapFrom(List<Stock> b) {
        return b.stream()
                .map(this::mapFrom)
                .toList();
    }


}
