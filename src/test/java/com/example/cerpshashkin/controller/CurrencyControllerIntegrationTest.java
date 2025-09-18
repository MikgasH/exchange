package com.example.cerpshashkin.controller;

import com.example.cerpshashkin.BaseWireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CurrencyControllerIntegrationTest extends BaseWireMockTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String createUrl(final String endpoint) {
        return "http://localhost:" + port + "/api/v1/currencies" + endpoint;
    }

    @Test
    void getCurrencies_ShouldReturnDefaultCurrencies() {
        final ResponseEntity<String> response = restTemplate.getForEntity(
                createUrl(""),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("USD", "EUR", "GBP");
    }

    @Test
    void addCurrency_WithValidCurrency_ShouldReturnSuccess() {
        final ResponseEntity<String> response = restTemplate.postForEntity(
                createUrl("?currency=NOK"),
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Currency NOK added successfully");

        final ResponseEntity<String> getCurrenciesResponse = restTemplate.getForEntity(
                createUrl(""),
                String.class
        );
        assertThat(getCurrenciesResponse.getBody()).contains("NOK");
    }

    @Test
    void addCurrency_WithInvalidCurrency_ShouldReturnError() {
        final ResponseEntity<String> response = restTemplate.postForEntity(
                createUrl("?currency=INVALID"),
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void deleteCurrency_WithExistingCurrency_ShouldReturnSuccess() {
        restTemplate.postForEntity(createUrl("?currency=SEK"), null, String.class);

        restTemplate.delete(createUrl("/SEK"));

        final ResponseEntity<String> response = restTemplate.getForEntity(
                createUrl(""),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).doesNotContain("SEK");
    }

    @Test
    void deleteCurrency_WithNonExistentCurrency_ShouldReturnError() {
        final ResponseEntity<String> response = restTemplate.exchange(
                createUrl("/NONEXISTENT"),
                org.springframework.http.HttpMethod.DELETE,
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void exchangeRates_WithSameCurrencies_ShouldReturnSameAmount() {
        final ResponseEntity<String> response = restTemplate.getForEntity(
                createUrl("/exchange-rates?amount=100&from=USD&to=USD"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
        assertThat(response.getBody()).contains("\"convertedAmount\":100");
        assertThat(response.getBody()).contains("\"exchangeRate\":1");
        assertThat(response.getBody()).contains("\"provider\":\"Same Currency\"");
    }

    @Test
    void exchangeRates_WithValidCurrencies_AndMockProvider_ShouldReturnConversion() {
        stubAllProvidersToFail();

        final ResponseEntity<String> response = restTemplate.getForEntity(
                createUrl("/exchange-rates?amount=100&from=USD&to=EUR"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
        assertThat(response.getBody()).contains("\"originalAmount\":100");
        assertThat(response.getBody()).contains("\"fromCurrency\":\"USD\"");
        assertThat(response.getBody()).contains("\"toCurrency\":\"EUR\"");
    }

    @Test
    void exchangeRates_WithSuccessfulExternalProvider_ShouldReturnConversion() {
        stubFor(get(urlEqualTo("/latest?access_key=test-fixer-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("fixer-exchangerates-success-response.json"))));

        final ResponseEntity<String> response = restTemplate.getForEntity(
                createUrl("/exchange-rates?amount=100&from=EUR&to=USD"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
        assertThat(response.getBody()).contains("\"originalAmount\":100");
    }

    @Test
    void exchangeRates_WithMissingParameters_ShouldReturnBadRequest() {
        final ResponseEntity<String> response = restTemplate.getForEntity(
                createUrl("/exchange-rates?amount=100"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void refreshRates_WithMockProvider_ShouldReturnSuccess() {
        stubAllProvidersToFail();

        final ResponseEntity<String> response = restTemplate.postForEntity(
                createUrl("/refresh"),
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Exchange rates updated successfully");
    }

    @Test
    void refreshRates_WithSuccessfulExternalProvider_ShouldReturnSuccess() {
        stubFor(get(urlEqualTo("/latest?access_key=test-fixer-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile("fixer-exchangerates-success-response.json"))));

        final ResponseEntity<String> response = restTemplate.postForEntity(
                createUrl("/refresh"),
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Exchange rates updated successfully");
    }

    @Test
    void fullWorkflow_AddConvertRefreshDelete_ShouldWorkCorrectly() {
        stubAllProvidersToFail();

        ResponseEntity<String> response = restTemplate.getForEntity(createUrl(""), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("USD", "EUR", "GBP");

        response = restTemplate.postForEntity(createUrl("?currency=JPY"), null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = restTemplate.getForEntity(createUrl(""), String.class);
        assertThat(response.getBody()).contains("JPY");

        response = restTemplate.postForEntity(createUrl("/refresh"), null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = restTemplate.getForEntity(
                createUrl("/exchange-rates?amount=100&from=USD&to=JPY"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        restTemplate.delete(createUrl("/JPY"));

        response = restTemplate.getForEntity(createUrl(""), String.class);
        assertThat(response.getBody()).doesNotContain("JPY");
    }

    private void stubAllProvidersToFail() {
        stubFor(get(urlPathMatching("/latest.*"))
                .willReturn(aResponse().withStatus(500)));
    }
}
