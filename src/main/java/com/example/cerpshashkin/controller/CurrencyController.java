package com.example.cerpshashkin.controller;

import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/currencies")
public class CurrencyController {

    @GetMapping("/exchange-rates")
    public CurrencyExchangeResponse getExchangeRates() {
        Map<String, Double> rates = Map.of(
                "CAD", 1.260046,
                "CHF", 0.933058,
                "EUR", 0.806942,
                "GBP", 0.719154,
                "JPY", 107.346001,
                "AUD", 1.355018
        );

        return new CurrencyExchangeResponse(
                true,
                System.currentTimeMillis() / 1000,
                "USD",
                LocalDate.now().toString(),
                rates
        );
    }

    @PostMapping
    public ResponseEntity<String> addCurrency(@RequestParam String currency) {
        return ResponseEntity.ok("Currency " + currency + " added");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshRates() {
        return ResponseEntity.ok("Exchange rates updated");
    }

    @DeleteMapping("/{currency}")
    public ResponseEntity<String> deleteCurrency(@PathVariable String currency) {
        return ResponseEntity.ok("Currency " + currency + " deleted");
    }
}
