package com.example.cerpshashkin.client.integration;

import com.example.cerpshashkin.BaseWireMockTest;
import com.example.cerpshashkin.client.impl.FixerioClient;
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

class FixerioClientIntegrationTest extends BaseWireMockTest {

    @Autowired
    private FixerioClient fixerioClient;

    @Test
    void getLatestRates_ShouldReturnSuccessfulResponse() {
        stubFor(get(urlEqualTo("/latest?access_key=test-fixer-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("fixer-exchangerates-success-response.json"))));

        CurrencyExchangeResponse result = fixerioClient.getLatestRates();

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.base()).isEqualTo(Currency.getInstance("EUR"));
        assertThat(result.rates()).containsKeys(
                Currency.getInstance("USD"),
                Currency.getInstance("GBP"),
                Currency.getInstance("JPY")
        );
    }

    @Test
    void getLatestRates_WithSymbols_ShouldReturnFilteredResponse() {
        String symbols = "USD,GBP";
        stubFor(get(urlEqualTo("/latest?access_key=test-fixer-key&symbols=USD,GBP"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("fixer-exchangerates-filtered-response.json"))));

        CurrencyExchangeResponse result = fixerioClient.getLatestRates(symbols);

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.rates()).hasSize(2);
    }

    @Test
    void getLatestRates_WhenServerReturns500_ShouldThrowException() {
        stubFor(get(urlEqualTo("/latest?access_key=test-fixer-key"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("error-response.json"))));

        assertThatThrownBy(() -> fixerioClient.getLatestRates())
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Failed to fetch latest exchange rates from Fixer.io");
    }
}
