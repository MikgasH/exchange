package com.example.cerpshashkin.controller;

import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import com.example.cerpshashkin.service.MockCurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for handling currency-related API requests.
 */
@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
@Slf4j
public class CurrencyController {

    /**
     * The service used to handle currency-related business logic.
     * It provides methods for fetching currency data and exchange rates.
     */
    private final MockCurrencyService mockCurrencyService;

    /**
     * Retrieves a list of all supported currencies.
     *
     * @return a ResponseEntity containing a list of currency codes.
     */
    @GetMapping
    public ResponseEntity<List<String>> getCurrencies() {
        log.info("Class CurrencyController method getCurrencies");
        List<String> currencies = mockCurrencyService.getSupportedCurrencies();
        return ResponseEntity.ok(currencies);
    }

    /**
     * Retrieves the current exchange rates for all supported currencies.
     *
     * @return a {@link CurrencyExchangeResponse} object containing the
     * exchange rates.
     */
    @GetMapping("/exchange-rates")
    public CurrencyExchangeResponse getExchangeRates() {
        log.info("Class CurrencyController method getExchangeRates");
        return mockCurrencyService.getExchangeRates();
    }

    /**
     * Adds a new currency to the system.
     *
     * @param currency The currency code to add.
     * @return a ResponseEntity indicating the success of the operation.
     */
    @PostMapping
    public ResponseEntity<String> addCurrency(@RequestParam final String currency) {
        log.info("Class CurrencyController method addCurrency with currency: {}", currency);
        return ResponseEntity.ok("Currency " + currency + " added");
    }

    /**
     * Refreshes the exchange rates.
     *
     * @return a ResponseEntity indicating the success of the operation.
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshRates() {
        log.info("Class CurrencyController method refreshRates");
        return ResponseEntity.ok("Exchange rates updated");
    }

    /**
     * Deletes a currency from the system.
     *
     * @param currency The currency code to delete.
     * @return a ResponseEntity indicating the success of the operation.
     */
    @DeleteMapping("/{currency}")
    public ResponseEntity<String> deleteCurrency(@PathVariable final String currency) {
        log.info("Class CurrencyController method deleteCurrency with currency: {}", currency);
        return ResponseEntity.ok("Currency " + currency + " deleted");
    }
}
