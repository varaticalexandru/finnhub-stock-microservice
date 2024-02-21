package org.alexv.stockservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alexv.stockservice.model.Currency;
import org.alexv.stockservice.model.Stock;
import org.alexv.stockservice.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebMvcTest(StockController.class)
public class StockControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    StockService stockService;

    private static final String TICKER = "test_ticker";
    private static final String FIGI = "figi";
    private static final String NAME = "name";
    private static final String TYPE = "Etf";
    private static final Currency CURRENCY = Currency.USD;
    private static final String SOURCE = "BATS";

    Stock actualStock = Stock.builder()
            .name(NAME)
            .ticker(TICKER)
            .source(SOURCE)
            .currency(CURRENCY)
            .type(TYPE)
            .figi(FIGI)
            .build();

    @BeforeEach
    void beforeEach() {
        when(stockService.getStockByTicker(any())).thenReturn(actualStock);
    }

    @Test
    void getStockByTicker() throws Exception {
        var result = mockMvc.perform(
                MockMvcRequestBuilders.get(String.format("/api/stocks/%s", TICKER))
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        verify(stockService, times(1)).getStockByTicker(anyString());
        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals(result, objectMapper.writeValueAsString(actualStock));
    }

}
