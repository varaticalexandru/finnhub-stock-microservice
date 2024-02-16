package org.alexv.stockservice.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Currency {
    RUB("RUB"),
    USD("USD"),
    EUR("EUR"),
    GBP("GBP"),
    HKD("HKD"),
    CHF("CHF"),
    JPY("JPY"),
    CNY("CNY"),
    TRY("TRY");

    private final String currency;

    public String currency() {
        return currency;
    }
}
