package org.alexv.stockservice.model;

import java.util.ArrayList;
import java.util.List;

public enum MIC {
    NEW_YORK_STOCK_EXCHANGE("XNYS"),
    OVER_THE_COUNTER("OOTC"),
    NYSE_AMERICAN("XASE"),
    CBOE_BZX("BATS"),
    NYSE_ARCA("ARCX"),
    NASDAQ("XNAS"),
    INVESTORS_EXCHANGE("IEXG");

    final String code;

    MIC(String code) {
        this.code = code;
    }

    public static List<String> getAll() {
        List<String> mics = new ArrayList<>();
        for (MIC mic : MIC.values()) {
            mics.add(mic.code);
        }
        return mics;
    }
}