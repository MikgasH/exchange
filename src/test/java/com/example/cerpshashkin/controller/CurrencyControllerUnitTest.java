package com.example.cerpshashkin.controller;

import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import com.example.cerpshashkin.service.MockCurrencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SuppressWarnings("deprecation")
@WebMvcTest(CurrencyController.class)
class CurrencyControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MockCurrencyService mockCurrencyService;

    @Test
    void getExchangeRates() throws Exception {
        Map<Currency, BigDecimal> rates = Map.of(
                Currency.getInstance("EUR"), BigDecimal.valueOf(0.85)
        );
        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(), rates
        );
        when(mockCurrencyService.getExchangeRates()).thenReturn(response);

        mockMvc.perform(get("/api/v1/currencies/exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void addCurrency() throws Exception {
        mockMvc.perform(post("/api/v1/currencies")
                        .param("currency", "EUR"))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Currency EUR added"));
    }

    @Test
    void deleteCurrency() throws Exception {
        mockMvc.perform(delete("/api/v1/currencies/USD"))
                .andExpect(status().isOk())
                .andExpect(content().string("Currency USD deleted"));
    }
}
