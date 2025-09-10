package com.example.cerpshashkin.client.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Base class for integration tests that use WireMock.
 * Provides a unified WireMock server on port 8080 with different endpoints for each API.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "external.api.fixer-url=http://localhost:8080/fixer",
        "external.api.fixer-access-key=test-fixer-key",
        "external.api.exchangerates-url=http://localhost:8080/exchangerates",
        "external.api.exchangerates-access-key=test-exchangerates-key",
        "external.api.currencyapi-url=http://localhost:8080/currencyapi",
        "external.api.currencyapi-access-key=test-currencyapi-key"
})
public abstract class BaseWireMockTest {

    protected WireMockServer wireMockServer;

    @BeforeEach
    void setUpWireMock() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8080);
    }

    @AfterEach
    void tearDownWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    /**
     * Common JSON response for Fixer.io format APIs (Fixer.io and ExchangeRatesAPI).
     */
    protected static final String FIXER_FORMAT_RESPONSE = """
            {
                "success": true,
                "timestamp": 1725459054,
                "base": "EUR",
                "date": "2025-09-04",
                "rates": {
                    "USD": 1.18,
                    "GBP": 0.87,
                    "JPY": 130.5
                }
            }""";

    /**
     * Common JSON response for CurrencyAPI format.
     */
    protected static final String CURRENCY_API_RESPONSE = """
            {
              "meta": {
                "last_updated_at": "2023-06-23T10:04:00Z"
              },
              "data": {
                "EUR": {
                  "code": "EUR",
                  "value": 0.85
                },
                "GBP": {
                  "code": "GBP",
                  "value": 0.75
                },
                "JPY": {
                  "code": "JPY",
                  "value": 110.0
                }
              }
            }""";

    /**
     * Filtered response for Fixer.io format APIs with specific symbols.
     */
    protected static final String FIXER_FORMAT_FILTERED_RESPONSE = """
            {
                "success": true,
                "timestamp": 1725459054,
                "base": "EUR",
                "date": "2025-09-04",
                "rates": {
                    "USD": 1.18,
                    "GBP": 0.87
                }
            }""";

    /**
     * Filtered response for CurrencyAPI format with specific currencies.
     */
    protected static final String CURRENCY_API_FILTERED_RESPONSE = """
            {
              "meta": {
                "last_updated_at": "2023-06-23T10:04:00Z"
              },
              "data": {
                "EUR": {
                  "code": "EUR",
                  "value": 0.85
                },
                "GBP": {
                  "code": "GBP",
                  "value": 0.75
                }
              }
            }""";

    /**
     * Error response for testing error scenarios.
     */
    protected static final String ERROR_RESPONSE = """
            {
                "error": "Internal Server Error"
            }""";
}
