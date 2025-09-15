package com.example.cerpshashkin.client.unit;

import com.example.cerpshashkin.client.BaseExternalClient;
import com.example.cerpshashkin.client.impl.FixerioClient;
import com.example.cerpshashkin.converter.ExternalApiConverter;
import com.example.cerpshashkin.dto.FixerioResponse;
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
class FixerioClientTest {

    @Mock
    private BaseExternalClient baseClient;

    @InjectMocks
    private FixerioClient fixerioClient;

    @Test
    void getLatestRates_ShouldCallBaseClientWithCorrectParameters() {
        ReflectionTestUtils.setField(fixerioClient, "fixerUrl", "http://test.url");
        ReflectionTestUtils.setField(fixerioClient, "fixerAccessKey", "test-key");

        CurrencyExchangeResponse expectedResponse = CurrencyExchangeResponse.success(
                Currency.getInstance("EUR"),
                LocalDate.now(),
                Map.of(Currency.getInstance("USD"), BigDecimal.valueOf(1.18))
        );

        when(baseClient.executeApiCall(
                anyString(),
                eq(FixerioResponse.class),
                any(),
                eq("Fixer.io"),
                eq("fetch latest exchange rates")
        )).thenReturn(expectedResponse);

        CurrencyExchangeResponse result = fixerioClient.getLatestRates();

        assertThat(result).isEqualTo(expectedResponse);
        verify(baseClient).executeApiCall(
                eq("http://test.url/latest?access_key=test-key"),
                eq(FixerioResponse.class),
                any(),
                eq("Fixer.io"),
                eq("fetch latest exchange rates")
        );
    }

    @Test
    void getLatestRates_WithSymbols_ShouldCallBaseClientWithSymbolsInUrl() {
        ReflectionTestUtils.setField(fixerioClient, "fixerUrl", "http://test.url");
        ReflectionTestUtils.setField(fixerioClient, "fixerAccessKey", "test-key");

        String symbols = "USD,EUR";
        CurrencyExchangeResponse expectedResponse = CurrencyExchangeResponse.success(
                Currency.getInstance("EUR"),
                LocalDate.now(),
                Map.of(Currency.getInstance("USD"), BigDecimal.valueOf(1.18))
        );

        when(baseClient.executeApiCall(
                anyString(),
                eq(FixerioResponse.class),
                any(),
                eq("Fixer.io"),
                eq("fetch exchange rates for symbols " + symbols)
        )).thenReturn(expectedResponse);

        CurrencyExchangeResponse result = fixerioClient.getLatestRates(symbols);

        assertThat(result).isEqualTo(expectedResponse);
        verify(baseClient).executeApiCall(
                eq("http://test.url/latest?access_key=test-key&symbols=USD,EUR"),
                eq(FixerioResponse.class),
                any(),
                eq("Fixer.io"),
                eq("fetch exchange rates for symbols " + symbols)
        );
    }

    @Test
    void getLatestRates_WithEmptySymbols_ShouldThrowException() {
        assertThatThrownBy(() -> fixerioClient.getLatestRates(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");
    }

    @Test
    void getLatestRates_WithNullSymbols_ShouldThrowException() {
        assertThatThrownBy(() -> fixerioClient.getLatestRates(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");
    }

    @Test
    void getProviderName_ShouldReturnCorrectName() {
        assertThat(fixerioClient.getProviderName()).isEqualTo("Fixer.io");
    }
}
