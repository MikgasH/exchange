package com.example.cerpshashkin.exception;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    void invalidCurrencyException_WithCurrencyCode_ShouldContainCode() {
        InvalidCurrencyException ex = new InvalidCurrencyException("XXX");
        assertThat(ex.getMessage()).contains("Invalid currency code: XXX");
    }

    @Test
    void invalidCurrencyException_WithMessage_ShouldContainMessage() {
        InvalidCurrencyException ex = new InvalidCurrencyException("Custom message");
        assertThat(ex.getMessage()).isEqualTo("Invalid currency code: Custom message");
    }

    @Test
    void currencyNotFoundException_ShouldContainCurrencyCode() {
        CurrencyNotFoundException ex = new CurrencyNotFoundException("JPY");
        assertThat(ex.getMessage()).contains("Currency not found: JPY");
    }

    @Test
    void allProvidersFailedException_ShouldListAllProviders() {
        List<String> providers = List.of("Fixer.io", "ExchangeRatesAPI", "CurrencyAPI");
        AllProvidersFailedException ex = new AllProvidersFailedException(providers);

        assertThat(ex.getMessage())
                .contains("All exchange rate providers failed")
                .contains("Fixer.io")
                .contains("ExchangeRatesAPI")
                .contains("CurrencyAPI");
    }

    @Test
    void exchangeRateNotAvailableException_WithTwoCurrencies_ShouldContainBoth() {
        ExchangeRateNotAvailableException ex =
                new ExchangeRateNotAvailableException("USD", "EUR");

        assertThat(ex.getMessage())
                .contains("Exchange rate not available for USD -> EUR");
    }

    @Test
    void exchangeRateNotAvailableException_WithMessage_ShouldContainMessage() {
        ExchangeRateNotAvailableException ex =
                new ExchangeRateNotAvailableException("Custom error");

        assertThat(ex.getMessage()).isEqualTo("Custom error");
    }

    @Test
    void rateNotAvailableException_ShouldContainCurrencyPair() {
        RateNotAvailableException ex = new RateNotAvailableException("GBP", "JPY");

        assertThat(ex.getMessage())
                .contains("Exchange rate not available for GBP -> JPY");
    }

    @Test
    void externalApiException_ShouldContainOperationAndProvider() {
        ExternalApiException ex = new ExternalApiException(
                "fetch latest rates",
                "Fixer.io",
                "HTTP 500"
        );

        assertThat(ex.getMessage())
                .contains("Failed to fetch latest rates from Fixer.io")
                .contains("HTTP 500");
    }
}
