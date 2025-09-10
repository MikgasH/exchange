package com.example.cerpshashkin.client.unit;

import com.example.cerpshashkin.client.impl.MockExchangeRateClient;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class MockExchangeRateClientTest {

    @InjectMocks
    private MockExchangeRateClient mockExchangeRateClient;

    @Test
    void getLatestRates_ShouldReturnSuccessfulResponse() {
        CurrencyExchangeResponse result = mockExchangeRateClient.getLatestRates();

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.base()).isEqualTo(Currency.getInstance("EUR"));
        assertThat(result.rates()).isNotEmpty();
        assertThat(result.lastUpdated()).isNotNull();
        assertThat(result.rateDate()).isNotNull();
    }

    @Test
    void getLatestRates_WithSymbols_ShouldReturnFilteredResponse() {
        String symbols = "USD,GBP";

        CurrencyExchangeResponse result = mockExchangeRateClient.getLatestRates(symbols);

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.rates()).hasSize(2);
        assertThat(result.rates()).containsKeys(
                Currency.getInstance("USD"),
                Currency.getInstance("GBP")
        );
    }

    @Test
    void getLatestRates_WithInvalidSymbols_ShouldReturnEmptyRates() {
        String symbols = "INVALID,CURRENCY";

        CurrencyExchangeResponse result = mockExchangeRateClient.getLatestRates(symbols);

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.rates()).isEmpty();
    }

    @Test
    void getLatestRates_WithEmptySymbols_ShouldThrowException() {
        assertThatThrownBy(() -> mockExchangeRateClient.getLatestRates(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");
    }

    @Test
    void getLatestRates_WithNullSymbols_ShouldThrowException() {
        assertThatThrownBy(() -> mockExchangeRateClient.getLatestRates(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");
    }

    @Test
    void getLatestRates_ShouldGenerateDifferentRatesOnMultipleCalls() {
        CurrencyExchangeResponse result1 = mockExchangeRateClient.getLatestRates();
        CurrencyExchangeResponse result2 = mockExchangeRateClient.getLatestRates();

        assertThat(result1.rates()).isNotEqualTo(result2.rates());
    }

    @Test
    void getLatestRates_ShouldIncludeTimestampAndDate() {
        CurrencyExchangeResponse result = mockExchangeRateClient.getLatestRates();

        assertThat(result.lastUpdated()).isNotNull();
        assertThat(result.rateDate()).isNotNull();
    }

    @Test
    void getLatestRates_WithValidSymbols_ShouldOnlyReturnRequestedCurrencies() {
        String symbols = "USD,JPY,CAD";

        CurrencyExchangeResponse result = mockExchangeRateClient.getLatestRates(symbols);

        assertThat(result.rates()).hasSize(3);
        assertThat(result.rates()).containsOnlyKeys(
                Currency.getInstance("USD"),
                Currency.getInstance("JPY"),
                Currency.getInstance("CAD")
        );
    }

    @Test
    void getProviderName_ShouldReturnCorrectName() {
        assertThat(mockExchangeRateClient.getProviderName()).isEqualTo("MockAPI");
    }
}
