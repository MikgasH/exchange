package com.example.cerpshashkin.service;

import com.example.cerpshashkin.BaseWireMockTest;
import com.example.cerpshashkin.service.cache.CurrencyRateCache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CacheExpirationIntegrationTest extends BaseWireMockTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private CurrencyRateCache cache;

    @Test
    void freshCache_ShouldNotCallProviders() {
        stubFor(get(urlEqualTo("/latest?access_key=test-fixer-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("fixer-exchangerates-success-response.json"))));

        exchangeRateService.refreshRates();

        wireMockServer.resetRequests();

        Currency eur = Currency.getInstance("EUR");
        Currency usd = Currency.getInstance("USD");

        Optional<BigDecimal> rate1 = exchangeRateService.getExchangeRate(eur, usd);
        Optional<BigDecimal> rate2 = exchangeRateService.getExchangeRate(eur, usd);
        Optional<BigDecimal> rate3 = exchangeRateService.getExchangeRate(eur, usd);

        assertThat(rate1).isPresent();
        assertThat(rate2).isPresent();
        assertThat(rate3).isPresent();

        verify(0, getRequestedFor(urlEqualTo("/latest?access_key=test-fixer-key")));
    }

    @Test
    void clearCache_ShouldForceApiCall() {
        stubFor(get(urlEqualTo("/latest?access_key=test-fixer-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("fixer-exchangerates-success-response.json"))));

        Currency eur = Currency.getInstance("EUR");
        Currency usd = Currency.getInstance("USD");
        BigDecimal initialRate = new BigDecimal("1.250000");

        cache.putRate(eur, usd, initialRate);

        wireMockServer.resetRequests();

        cache.clearCache();

        Optional<BigDecimal> rate = exchangeRateService.getExchangeRate(eur, usd);

        verify(1, getRequestedFor(urlEqualTo("/latest?access_key=test-fixer-key")));
        assertThat(rate).isPresent();
    }
}
