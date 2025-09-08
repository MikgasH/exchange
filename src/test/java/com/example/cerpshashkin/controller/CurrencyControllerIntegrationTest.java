package com.example.cerpshashkin.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/*
Difference between Unit and Integration tests:

Unit tests:
- Test only controller separately
- Use fake dependencies (mocks)
- Work fast
- Check logic of one component

Integration tests:
- Test entire system as a whole
- Start full application
- Use real components (not mocks)
- Slower, but check that everything works together
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CurrencyControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String createUrl(String endpoint) {
        return "http://localhost:" + port + "/api/v1/currencies" + endpoint;
    }

    @Test
    void getExchangeRates() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                createUrl("/exchange-rates"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
        assertThat(response.getBody()).contains("USD");
    }

    @Test
    void addCurrency() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                createUrl("?currency=EUR"),
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Currency EUR added");
    }

    @Test
    void deleteCurrency() {
        restTemplate.delete(createUrl("/USD"));

        ResponseEntity<String> response = restTemplate.getForEntity(
                createUrl("/exchange-rates"),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getCurrencies() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                createUrl(""),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void refreshRates() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                createUrl("/refresh"),
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Exchange rates updated");
    }
}
