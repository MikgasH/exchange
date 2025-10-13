package com.example.cerpshashkin.controller;

import com.example.cerpshashkin.dto.ConversionRequest;
import com.example.cerpshashkin.dto.ConversionResponse;
import com.example.cerpshashkin.exception.CurrencyNotSupportedException;
import com.example.cerpshashkin.exception.RateNotAvailableException;
import com.example.cerpshashkin.service.CurrencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrencyService currencyService;

    @Test
    void getCurrencies_ShouldReturnListOfCurrencies_WhenServiceReturnsData() throws Exception {
        List<String> currencies = List.of("USD", "EUR", "GBP");
        when(currencyService.getSupportedCurrencies()).thenReturn(currencies);

        mockMvc.perform(get("/api/v1/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0]").value("USD"))
                .andExpect(jsonPath("$[1]").value("EUR"))
                .andExpect(jsonPath("$[2]").value("GBP"));

        verify(currencyService, times(1)).getSupportedCurrencies();
    }

    @Test
    void getCurrencies_ShouldReturnEmptyList_WhenNoSupportedCurrencies() throws Exception {
        when(currencyService.getSupportedCurrencies()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(currencyService, times(1)).getSupportedCurrencies();
    }

    @Test
    void addCurrency_ShouldReturnSuccessMessage_WhenServiceSucceeds() throws Exception {
        String currency = "NOK";
        doNothing().when(currencyService).addCurrency(currency);

        mockMvc.perform(post("/api/v1/currencies")
                        .param("currency", currency))
                .andExpect(status().isOk())
                .andExpect(content().string("Currency NOK added successfully"));

        verify(currencyService, times(1)).addCurrency(currency);
    }

    @Test
    void addCurrency_ShouldReturnBadRequest_WhenCurrencyParameterIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/currencies"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Missing required parameter"));

        verify(currencyService, never()).addCurrency(any());
    }

    @Test
    void addCurrency_ShouldReturnBadRequest_WhenCurrencyIsInvalid() throws Exception {
        String currency = "INVALID";

        mockMvc.perform(post("/api/v1/currencies")
                        .param("currency", currency))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.detail").value("addCurrency.currency: Invalid currency code"));

        verify(currencyService, never()).addCurrency(any());
    }

    @Test
    void getExchangeRates_ShouldReturnSuccessResponse_WhenConversionIsSuccessful() throws Exception {
        BigDecimal amount = BigDecimal.valueOf(100);
        String from = "USD";
        String to = "EUR";
        ConversionResponse successResponse = ConversionResponse.success(
                amount, from, to,
                BigDecimal.valueOf(85.0),
                BigDecimal.valueOf(0.85),
                "test-provider"
        );

        doNothing().when(currencyService).validateSupportedCurrencies(from, to);
        when(currencyService.convertCurrency(any(ConversionRequest.class)))
                .thenReturn(successResponse);

        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                        .param("amount", "100")
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.originalAmount").value(100))
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("EUR"))
                .andExpect(jsonPath("$.convertedAmount").value(85.0))
                .andExpect(jsonPath("$.exchangeRate").value(0.85))
                .andExpect(jsonPath("$.provider").value("test-provider"));

        verify(currencyService, times(1)).validateSupportedCurrencies(from, to);
        verify(currencyService, times(1)).convertCurrency(any(ConversionRequest.class));
    }

    @Test
    void getExchangeRates_ShouldReturnBadRequest_WhenCurrencyNotSupported() throws Exception {
        String from = "USD";
        String to = "XAU";

        doNothing().when(currencyService).validateSupportedCurrencies(from, to);
        doThrow(new CurrencyNotSupportedException("XAU", List.of("USD", "EUR", "GBP")))
                .when(currencyService).convertCurrency(any(ConversionRequest.class));

        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                        .param("amount", "100")
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Currency Not Supported"))
                .andExpect(jsonPath("$.detail").value("Currency 'XAU' is not supported. Available currencies: USD, EUR, GBP"));

        verify(currencyService, times(1)).validateSupportedCurrencies(from, to);
        verify(currencyService, times(1)).convertCurrency(any(ConversionRequest.class));
    }

    @Test
    void getExchangeRates_ShouldReturnBadRequest_WhenToInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                        .param("amount", "100")
                        .param("from", "USD")
                        .param("to", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.detail").value("to: Invalid target currency code"));

        verify(currencyService, never()).validateSupportedCurrencies(anyString(), anyString());
    }

    @Test
    void getExchangeRates_ShouldReturnServiceUnavailable_WhenRateNotAvailable() throws Exception {
        String from = "USD";
        String to = "EUR";

        doNothing().when(currencyService).validateSupportedCurrencies(from, to);
        when(currencyService.convertCurrency(any(ConversionRequest.class)))
                .thenThrow(new RateNotAvailableException("USD", "EUR"));

        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                        .param("amount", "100")
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Exchange rate not available"))
                .andExpect(jsonPath("$.detail").value("Exchange rate not available for USD -> EUR"));

        verify(currencyService, times(1)).validateSupportedCurrencies(from, to);
        verify(currencyService, times(1)).convertCurrency(any(ConversionRequest.class));
    }

    @Test
    void getExchangeRates_ShouldReturnBadRequest_WhenRequiredParametersAreMissing() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                        .param("amount", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"));

        verify(currencyService, never()).validateSupportedCurrencies(anyString(), anyString());
        verify(currencyService, never()).convertCurrency(any());
    }

    @Test
    void getExchangeRates_ShouldReturnBadRequest_WhenAmountIsNegative() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                        .param("amount", "-100")
                        .param("from", "USD")
                        .param("to", "EUR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"));

        verify(currencyService, never()).validateSupportedCurrencies(anyString(), anyString());
        verify(currencyService, never()).convertCurrency(any());
    }

    @Test
    void getExchangeRates_ShouldReturnBadRequest_WhenAmountIsZero() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                        .param("amount", "0")
                        .param("from", "USD")
                        .param("to", "EUR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"));

        verify(currencyService, never()).validateSupportedCurrencies(anyString(), anyString());
        verify(currencyService, never()).convertCurrency(any());
    }

    @Test
    void getExchangeRates_ShouldReturnBadRequest_WhenFromCurrencyIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                        .param("amount", "100")
                        .param("from", "INVALID")
                        .param("to", "EUR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.detail").value("from: Invalid source currency code"));

        verify(currencyService, never()).validateSupportedCurrencies(anyString(), anyString());
        verify(currencyService, never()).convertCurrency(any());
    }

    @Test
    void refreshRates_ShouldReturnSuccessMessage_WhenServiceSucceeds() throws Exception {
        doNothing().when(currencyService).refreshExchangeRates();

        mockMvc.perform(post("/api/v1/currencies/refresh"))
                .andExpect(status().isOk())
                .andExpect(content().string("Exchange rates updated successfully"));

        verify(currencyService, times(1)).refreshExchangeRates();
    }
}
