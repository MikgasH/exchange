package com.example.cerpshashkin.client.unit;

import com.example.cerpshashkin.client.BaseExternalClient;
import com.example.cerpshashkin.client.impl.CurrencyApiClient;
import com.example.cerpshashkin.converter.CurrencyApiConverter;
import com.example.cerpshashkin.dto.CurrencyApiRawResponse;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyApiClientTest {

    @Mock
    private BaseExternalClient baseClient;

    @InjectMocks
    private CurrencyApiClient currencyApiClient;

    @Test
    void getLatestRates_ShouldCallBaseClientWithCorrectParameters() {
        ReflectionTestUtils.setField(currencyApiClient, "currencyapiUrl", "http://test.url");
        ReflectionTestUtils.setField(currencyApiClient, "currencyapiAccessKey", "test-key");

        CurrencyExchangeResponse expectedResponse = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"),
                LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.85))
        );

        when(baseClient.executeApiCall(
                anyString(),
                eq(CurrencyApiRawResponse.class),
                any(),
                eq("CurrencyAPI"),
                eq("fetch latest exchange rates")
        )).thenReturn(expectedResponse);

        CurrencyExchangeResponse result = currencyApiClient.getLatestRates();

        assertThat(result).isEqualTo(expectedResponse);
        verify(baseClient).executeApiCall(
                eq("http://test.url/latest?apikey=test-key"),
                eq(CurrencyApiRawResponse.class),
                any(),
                eq("CurrencyAPI"),
                eq("fetch latest exchange rates")
        );
    }

    @Test
    void getLatestRates_WithSymbols_ShouldCallBaseClientWithCurrenciesInUrl() {
        ReflectionTestUtils.setField(currencyApiClient, "currencyapiUrl", "http://test.url");
        ReflectionTestUtils.setField(currencyApiClient, "currencyapiAccessKey", "test-key");

        String symbols = "EUR,GBP";
        CurrencyExchangeResponse expectedResponse = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"),
                LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.85))
        );

        when(baseClient.executeApiCall(
                anyString(),
                eq(CurrencyApiRawResponse.class),
                any(),
                eq("CurrencyAPI"),
                eq("fetch exchange rates for symbols " + symbols)
        )).thenReturn(expectedResponse);

        CurrencyExchangeResponse result = currencyApiClient.getLatestRates(symbols);

        assertThat(result).isEqualTo(expectedResponse);
        verify(baseClient).executeApiCall(
                eq("http://test.url/latest?apikey=test-key&currencies=EUR,GBP"),
                eq(CurrencyApiRawResponse.class),
                any(),
                eq("CurrencyAPI"),
                eq("fetch exchange rates for symbols " + symbols)
        );
    }

    @Test
    void getLatestRates_WithEmptySymbols_ShouldThrowException() {
        assertThatThrownBy(() -> currencyApiClient.getLatestRates(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");
    }

    @Test
    void getLatestRates_WithNullSymbols_ShouldThrowException() {
        assertThatThrownBy(() -> currencyApiClient.getLatestRates(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");
    }

    @Test
    void getProviderName_ShouldReturnCorrectName() {
        assertThat(currencyApiClient.getProviderName()).isEqualTo("CurrencyAPI");
    }
}
