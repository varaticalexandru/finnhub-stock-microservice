package org.alexv.stockservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Stock {
    String ticker;
    String figi;
    String name;    // description
    String type;
    Currency currency;
    String source;  // exchange's mic
}
