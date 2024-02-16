package org.alexv.stockservice.mapper.impl;

import lombok.AllArgsConstructor;
import org.alexv.finnhubclient.model.Quote;
import org.alexv.stockservice.dto.StockPriceDto;
import org.alexv.stockservice.mapper.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class StockPriceMapper implements Mapper<Quote, StockPriceDto> {

    private ModelMapper modelMapper;

    @Override
    public StockPriceDto mapTo(Quote quote) {
        return modelMapper.map(quote, StockPriceDto.class);
    }

    @Override
    public Quote mapFrom(StockPriceDto stockPriceDto) {
        return modelMapper.map(stockPriceDto, Quote.class);
    }

    @Override
    public List<StockPriceDto> mapTo(List<Quote> a) {
        return a.stream()
                .map(this::mapTo)
                .toList();
    }

    @Override
    public List<Quote> mapFrom(List<StockPriceDto> b) {
        return b.stream()
                .map(this::mapFrom)
                .toList();
    }
}
