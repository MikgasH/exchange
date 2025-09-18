package com.example.cerpshashkin.service;

import com.example.cerpshashkin.BaseWireMockTest;
import com.example.cerpshashkin.dto.ConversionRequest;
import com.example.cerpshashkin.dto.ConversionResponse;
import com.example.cerpshashkin.exception.CurrencyNotFoundException;
import com.example.cerpshashkin.exception.InvalidCurrencyException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CurrencyServiceIntegrationTest extends BaseWireMockTest {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Test
    void getSupportedCurrencies_ShouldReturnInitialCurrencies() {
        assertThat(currencyService.getSupportedCurrencies())
                .containsExactlyInAnyOrder("USD", "EUR", "GBP")
                .isSorted();
    }

    @Test
    void addAndRemoveCurrency_ShouldWorkCorrectly() {
        currencyService.addCurrency("NOK");

        assertThat(currencyService.getSupportedCurrencies())
                .contains("NOK")
                .hasSize(4);

        currencyService.removeCurrency("NOK");

        assertThat(currencyService.getSupportedCurrencies())
                .doesNotContain("NOK")
                .hasSize(3);
    }

    @Test
    void addCurrency_WithInvalidCurrency_ShouldThrowException() {
        assertThatThrownBy(() -> currencyService.addCurrency("INVALID"))
                .isInstanceOf(InvalidCurrencyException.class);
    }

    @Test
    void removeCurrency_WithNonExistentCurrency_ShouldThrowException() {
        assertThatThrownBy(() -> currencyService.removeCurrency("NOTFOUND"))
                .isInstanceOf(CurrencyNotFoundException.class);
    }

    @Test
    void convertCurrency_WithSameCurrencies_ShouldReturnSameAmount() {
        ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from("USD")
                .to("USD")
                .build();

        ConversionResponse result = currencyService.convertCurrency(request);

        assertThat(result)
                .satisfies(response -> {
                    assertThat(response.success()).isTrue();
                    assertThat(response.convertedAmount()).isEqualTo(BigDecimal.valueOf(100));
                    assertThat(response.exchangeRate()).isEqualTo(BigDecimal.ONE);
                    assertThat(response.provider()).isEqualTo("Same Currency");
                });
    }

    @Test
    void convertCurrency_WithMockProvider_ShouldReturnConversion() {
        stubAllProvidersToFail();

        ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from("USD")
                .to("EUR")
                .build();

        ConversionResponse result = currencyService.convertCurrency(request);

        assertThat(result)
                .satisfies(response -> {
                    assertThat(response.success()).isTrue();
                    assertThat(response.originalAmount()).isEqualTo(BigDecimal.valueOf(100));
                    assertThat(response.fromCurrency()).isEqualTo("USD");
                    assertThat(response.toCurrency()).isEqualTo("EUR");
                    assertThat(response.convertedAmount()).isNotNull();
                    assertThat(response.exchangeRate()).isNotNull();
                });
    }

    @Test
    void convertCurrency_WithSuccessfulExternalProvider_ShouldReturnConversion() {
        stubFor(get(urlEqualTo("/latest?access_key=test-fixer-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("fixer-exchangerates-success-response.json"))));

        ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from("EUR")
                .to("USD")
                .build();

        ConversionResponse result = currencyService.convertCurrency(request);

        assertThat(result)
                .satisfies(response -> {
                    assertThat(response.success()).isTrue();
                    assertThat(response.originalAmount()).isEqualTo(BigDecimal.valueOf(100));
                    assertThat(response.convertedAmount()).isNotNull();
                    assertThat(response.exchangeRate()).isNotNull();
                });
    }

    @Test
    void refreshExchangeRates_WithMockProvider_ShouldSucceed() {
        stubAllProvidersToFail();

        currencyService.refreshExchangeRates();

        assertThat(currencyService.getSupportedCurrencies()).isNotEmpty();
    }

    @Test
    void refreshExchangeRates_WithExternalProvider_ShouldSucceed() {
        stubFor(get(urlEqualTo("/latest?access_key=test-fixer-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("fixer-exchangerates-success-response.json"))));

        currencyService.refreshExchangeRates();

        assertThat(currencyService.getSupportedCurrencies()).isNotEmpty();
    }

    @Test
    void fullWorkflow_AddConvertRefresh_ShouldWorkCorrectly() {
        stubAllProvidersToFail();

        assertThat(currencyService.getSupportedCurrencies()).hasSize(3);

        currencyService.addCurrency("JPY");
        assertThat(currencyService.getSupportedCurrencies()).hasSize(4);

        ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(1000))
                .from("USD")
                .to("JPY")
                .build();

        ConversionResponse result = currencyService.convertCurrency(request);
        assertThat(result.success()).isTrue();

        currencyService.refreshExchangeRates();

        ConversionResponse result2 = currencyService.convertCurrency(request);
        assertThat(result2.success()).isTrue();

        currencyService.removeCurrency("JPY");
        assertThat(currencyService.getSupportedCurrencies()).hasSize(3);
    }

    @Test
    void convertCurrency_WithInvalidCurrency_ShouldReturnFailure() {
        ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from("INVALID")
                .to("EUR")
                .build();

        assertThat(currencyService.convertCurrency(request).success()).isFalse();
    }

    @Test
    void exchangeRateService_GetLatestRates_WithMockProvider_ShouldSucceed() {
        stubAllProvidersToFail();

        var result = exchangeRateService.getLatestRates();

        assertThat(result)
                .satisfies(response -> {
                    assertThat(response.success()).isTrue();
                    assertThat(response.base()).isNotNull();
                    assertThat(response.rates()).isNotNull();
                });
    }

    @Test
    void exchangeRateService_GetLatestRates_WithSymbols_ShouldSucceed() {
        stubAllProvidersToFail();

        var result = exchangeRateService.getLatestRates("EUR,GBP");

        assertThat(result)
                .satisfies(response -> {
                    assertThat(response.success()).isTrue();
                    assertThat(response.rates()).isNotNull();
                });
    }

    @Test
    void exchangeRateService_RefreshRates_ShouldClearCacheAndFetchNew() {
        stubAllProvidersToFail();

        exchangeRateService.refreshRates();

        assertThat(exchangeRateService.getLatestRates().success()).isTrue();
    }

    private void stubAllProvidersToFail() {
        stubFor(get(urlPathMatching("/latest.*"))
                .willReturn(aResponse().withStatus(500)));
    }
}
