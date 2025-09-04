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

@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
@Slf4j
public class CurrencyController {

    private final MockCurrencyService mockCurrencyService;

    @GetMapping("/exchange-rates")
    public CurrencyExchangeResponse getExchangeRates() {
        log.info("Class CurrencyController method getExchangeRates");
        return mockCurrencyService.getExchangeRates();
    }

    @PostMapping
    public ResponseEntity<String> addCurrency(@RequestParam String currency) {
        log.info("Class CurrencyController method addCurrency with currency: {}", currency);
        return ResponseEntity.ok("Currency " + currency + " added");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshRates() {
        log.info("Class CurrencyController method refreshRates");
        return ResponseEntity.ok("Exchange rates updated");
    }

    @DeleteMapping("/{currency}")
    public ResponseEntity<String> deleteCurrency(@PathVariable String currency) {
        log.info("Class CurrencyController method deleteCurrency with currency: {}", currency);
        return ResponseEntity.ok("Currency " + currency + " deleted");
    }
}
