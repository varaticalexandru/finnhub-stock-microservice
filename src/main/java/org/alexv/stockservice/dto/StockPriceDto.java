package org.alexv.stockservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockPriceDto {
    private String ticker;

    private Double currentPrice;
    private Double highestPrice;
    private Double lowestPrice;
    private Double openinngPrice;
    private Double previousClose;
    private Double change;
    private Double percentChange;
}
