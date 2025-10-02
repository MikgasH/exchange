package com.example.cerpshashkin.scheduler;

import com.example.cerpshashkin.BaseWireMockTest;
import com.example.cerpshashkin.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SchedulerIntegrationTest extends BaseWireMockTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Test
    void applicationStartup_ShouldLoadExchangeRatesIntoCache() {
        stubFor(get(urlEqualTo("/latest?access_key=test-fixer-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("fixer-exchangerates-success-response.json"))));

        exchangeRateService.refreshRates();

        Currency eur = Currency.getInstance("EUR");
        Currency usd = Currency.getInstance("USD");

        Optional<BigDecimal> rate = exchangeRateService.getExchangeRate(eur, usd);

        assertThat(rate).isPresent();
        assertThat(rate.get()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void applicationStartup_WithAllProvidersFailed_ShouldUseMockProvider() {
        stubFor(get(urlPathMatching("/latest.*"))
                .willReturn(aResponse().withStatus(500)));

        exchangeRateService.refreshRates();

        Currency eur = Currency.getInstance("EUR");
        Currency usd = Currency.getInstance("USD");

        Optional<BigDecimal> rate = exchangeRateService.getExchangeRate(eur, usd);

        assertThat(rate).isPresent();
        assertThat(rate.get()).isGreaterThan(BigDecimal.ZERO);
    }
}
