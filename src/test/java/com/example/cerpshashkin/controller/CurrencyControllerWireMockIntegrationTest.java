package com.example.cerpshashkin.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CurrencyControllerWireMockIntegrationTest {

    private static final String WIREMOCK_VERSION = "3.3.1";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    //'GenericContainer<SELF>' used without 'try'-with-resources statement
    @SuppressWarnings("resource")
    @Container
    static GenericContainer<?> wireMockContainer = new GenericContainer<>("wiremock/wiremock:" + WIREMOCK_VERSION)
            .withExposedPorts(8080)
            .withCommand("--port", "8080");

    @BeforeEach
    void setUp() {
        String wireMockHost = wireMockContainer.getHost();
        Integer wireMockPort = wireMockContainer.getMappedPort(8080);

        configureFor(wireMockHost, wireMockPort);

        stubFor(get(urlEqualTo("/api/latest"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                            "success": true,
                            "lastUpdated": 1725459054,
                            "base": "USD",
                            "rateDate": "2025-09-04",
                            "rates": {
                                "EUR": 0.85,
                                "GBP": 0.75,
                                "JPY": 110.0
                            }
                        }
                        """)));
    }

    @AfterEach
    void tearDown() {
        reset();
    }

    private String createUrl(String endpoint) {
        return "http://localhost:" + port + "/api/v1/currencies" + endpoint;
    }

    @Test
    void getExchangeRates_ShouldReturnMockedData() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                createUrl("/exchange-rates"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
        assertThat(response.getBody()).contains("USD");
    }

    @Test
    void addCurrency_ShouldWorkWithExternalApiMock() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                createUrl("?currency=EUR"),
                null,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Currency EUR added");
    }

    @Test
    void refreshRates_ShouldTriggerExternalApiCall() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                createUrl("/refresh"),
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Exchange rates updated");
    }
}
