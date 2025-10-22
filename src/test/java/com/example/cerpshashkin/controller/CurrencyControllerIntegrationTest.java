package com.example.cerpshashkin.controller;

import com.example.cerpshashkin.BaseWireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CurrencyControllerIntegrationTest extends BaseWireMockTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/currencies")
                .build();
    }

    @Test
    void getCurrencies_ShouldReturnDefaultCurrencies() {
        final String response = restClient.get()
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, httpResponse) -> {})
                .body(String.class);

        assertThat(response)
                .contains("USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "CNY", "SEK", "NZD");
    }

    @Test
    void addCurrency_WithValidCurrency_ShouldReturnSuccess() {
        final String response = restClient.post()
                .uri("?currency=NOK")
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, httpResponse) -> {})
                .body(String.class);

        assertThat(response).contains("Currency NOK added successfully");

        final String getCurrenciesResponse = restClient.get()
                .retrieve()
                .body(String.class);

        assertThat(getCurrenciesResponse).contains("NOK");
    }

    @Test
    void addCurrency_WithInvalidCurrency_ShouldReturnBadRequest() {
        restClient.post()
                .uri("?currency=INVALID")
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body)
                            .contains("Validation error")
                            .contains("Invalid currency code");
                })
                .toBodilessEntity();
    }

    @Test
    void addCurrency_WithEmptyCurrency_ShouldReturnBadRequest() {
        restClient.post()
                .uri("?currency=")
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body).contains("Validation error");
                })
                .toBodilessEntity();
    }

    @Test
    void exchangeRates_WithSameCurrencies_ShouldReturnSameAmount() {
        final String response = restClient.get()
                .uri("/exchange-rates?amount=100&from=USD&to=USD")
                .retrieve()
                .body(String.class);

        assertThat(response)
                .contains("\"success\":true")
                .contains("\"convertedAmount\":100")
                .contains("\"exchangeRate\":1");
    }

    @Test
    void exchangeRates_WithValidCurrencies_AndMockProvider_ShouldReturnConversion() {
        wireMockServer.resetAll();

        stubFor(get(urlPathMatching("/latest.*"))
                .willReturn(aResponse().withStatus(500)));

        setupMockService1Stub();
        setupMockService2Stub();

        final String response = restClient.get()
                .uri("/exchange-rates?amount=100&from=USD&to=EUR")
                .retrieve()
                .body(String.class);

        assertThat(response)
                .contains("\"success\":true")
                .contains("\"originalAmount\":100")
                .contains("\"fromCurrency\":\"USD\"")
                .contains("\"toCurrency\":\"EUR\"");
    }

    @Test
    void exchangeRates_WithSuccessfulExternalProvider_ShouldReturnConversion() {
        final String response = restClient.get()
                .uri("/exchange-rates?amount=100&from=EUR&to=USD")
                .retrieve()
                .body(String.class);

        assertThat(response)
                .contains("\"success\":true")
                .contains("\"originalAmount\":100");
    }

    @Test
    void exchangeRates_WithUnsupportedFromCurrency_ShouldReturnBadRequest() {
        restClient.get()
                .uri("/exchange-rates?amount=100&from=INVALID&to=EUR")
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body)
                            .contains("Validation error")
                            .contains("Invalid source currency code");
                })
                .toBodilessEntity();
    }

    @Test
    void exchangeRates_WithInvalidFromCurrency_ShouldReturnBadRequest() {
        restClient.get()
                .uri("/exchange-rates?amount=100&from=INVALID&to=EUR")
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body)
                            .contains("Validation error")
                            .contains("Invalid source currency code");
                })
                .toBodilessEntity();
    }

    @Test
    void exchangeRates_WithInvalidToCurrency_ShouldReturnBadRequest() {
        restClient.get()
                .uri("/exchange-rates?amount=100&from=USD&to=XYZ")
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body)
                            .contains("Validation error")
                            .contains("Invalid target currency code");
                })
                .toBodilessEntity();
    }

    @Test
    void exchangeRates_WithNegativeAmount_ShouldReturnBadRequest() {
        restClient.get()
                .uri("/exchange-rates?amount=-100&from=USD&to=EUR")
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, httpResponse) -> {
                    assertThat(httpResponse.getStatusCode().value()).isEqualTo(400);
                })
                .toBodilessEntity();
    }

    @Test
    void exchangeRates_WithZeroAmount_ShouldReturnBadRequest() {
        restClient.get()
                .uri("/exchange-rates?amount=0&from=USD&to=EUR")
                .retrieve()
                .onStatus(status -> status.value() == 400, (request, httpResponse) -> {
                    assertThat(httpResponse.getStatusCode().value()).isEqualTo(400);
                })
                .toBodilessEntity();
    }

    @Test
    void refreshRates_WithMockProvider_ShouldReturnSuccess() {
        wireMockServer.resetAll();

        stubFor(get(urlPathMatching("/latest.*"))
                .willReturn(aResponse().withStatus(500)));

        setupMockService1Stub();
        setupMockService2Stub();

        final String response = restClient.post()
                .uri("/refresh")
                .retrieve()
                .body(String.class);

        assertThat(response).contains("Exchange rates updated successfully");
    }

    @Test
    void refreshRates_WithSuccessfulExternalProvider_ShouldReturnSuccess() {
        final String response = restClient.post()
                .uri("/refresh")
                .retrieve()
                .body(String.class);

        assertThat(response).contains("Exchange rates updated successfully");
    }

    @Test
    void fullWorkflow_AddConvertRefreshDelete_ShouldWorkCorrectly() {
        wireMockServer.resetAll();

        stubFor(get(urlPathMatching("/latest.*"))
                .willReturn(aResponse().withStatus(500)));

        setupMockService1Stub();
        setupMockService2Stub();

        String response = restClient.get().retrieve().body(String.class);
        assertThat(response).contains("USD", "EUR", "GBP");

        restClient.post().uri("?currency=SEK").retrieve().toBodilessEntity();

        response = restClient.get().retrieve().body(String.class);
        assertThat(response).contains("SEK");

        restClient.post().uri("/refresh").retrieve().toBodilessEntity();

        response = restClient.get()
                .uri("/exchange-rates?amount=100&from=EUR&to=SEK")
                .retrieve()
                .body(String.class);
        assertThat(response).contains("\"success\":true");
    }
}
