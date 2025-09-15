package com.example.cerpshashkin.client.integration;

import com.example.cerpshashkin.client.impl.CurrencyApiClient;
import com.example.cerpshashkin.exception.ExternalApiException;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Currency;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyApiClientIntegrationTest extends BaseWireMockTest {

    @Autowired
    private CurrencyApiClient currencyApiClient;

    @Test
    void getLatestRates_ShouldReturnSuccessfulResponse() {
        stubFor(get(urlEqualTo("/currencyapi/latest?apikey=test-currencyapi-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("currencyapi-success-response.json"))));

        CurrencyExchangeResponse result = currencyApiClient.getLatestRates();

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.base()).isEqualTo(Currency.getInstance("USD"));
        assertThat(result.rates()).containsKeys(
                Currency.getInstance("EUR"),
                Currency.getInstance("GBP"),
                Currency.getInstance("JPY")
        );
        assertThat(result.lastUpdated()).isNotNull();
        assertThat(result.rateDate()).isNotNull();
    }

    @Test
    void getLatestRates_WithSymbols_ShouldReturnFilteredResponse() {
        String symbols = "EUR,GBP";
        stubFor(get(urlEqualTo("/currencyapi/latest?apikey=test-currencyapi-key&currencies=EUR,GBP"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("currencyapi-filtered-response.json"))));

        CurrencyExchangeResponse result = currencyApiClient.getLatestRates(symbols);

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.rates()).hasSize(2);
        assertThat(result.rates()).containsKeys(
                Currency.getInstance("EUR"),
                Currency.getInstance("GBP")
        );
    }

    @Test
    void getLatestRates_WhenApiReturnsEmptyData_ShouldHandleGracefully() {
        stubFor(get(urlEqualTo("/currencyapi/latest?apikey=test-currencyapi-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("currencyapi-empty-response.json"))));

        CurrencyExchangeResponse result = currencyApiClient.getLatestRates();

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.rates()).isEmpty();
    }

    @Test
    void getLatestRates_WhenServerReturns500_ShouldThrowException() {
        stubFor(get(urlEqualTo("/currencyapi/latest?apikey=test-currencyapi-key"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("error-response.json"))));

        assertThatThrownBy(() -> currencyApiClient.getLatestRates())
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Failed to fetch latest exchange rates from CurrencyAPI");
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
    void getLatestRates_WithInvalidSymbols_ShouldHandleGracefully() {
        String symbols = "INVALID,NOTREAL";
        stubFor(get(urlEqualTo("/currencyapi/latest?apikey=test-currencyapi-key&currencies=INVALID,NOTREAL"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("currencyapi-empty-response.json"))));

        CurrencyExchangeResponse result = currencyApiClient.getLatestRates(symbols);

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.rates()).isEmpty();
    }

    @Test
    void getProviderName_ShouldReturnCorrectName() {
        assertThat(currencyApiClient.getProviderName()).isEqualTo("CurrencyAPI");
    }
}
