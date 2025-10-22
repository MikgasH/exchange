package com.example.mockservice2.controller;

import com.example.mockservice2.dto.ExchangeRateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/rates")
@Slf4j
public class ExchangeRateController {

    private static final String BASE_CURRENCY = "EUR";
    private static final Random RANDOM = new Random();

    private static final Map<String, BigDecimal> BASE_RATES = Map.of(
            "USD", BigDecimal.valueOf(1.08),
            "GBP", BigDecimal.valueOf(0.85),
            "JPY", BigDecimal.valueOf(160.0),
            "CHF", BigDecimal.valueOf(0.97),
            "CAD", BigDecimal.valueOf(1.5),
            "AUD", BigDecimal.valueOf(1.65),
            "CNY", BigDecimal.valueOf(7.8),
            "SEK", BigDecimal.valueOf(11.5),
            "NZD", BigDecimal.valueOf(1.7),
            "BYN", BigDecimal.valueOf(3.5)
    );

    @GetMapping("/latest")
    public ResponseEntity<ExchangeRateResponse> getLatestRates(
            @RequestParam(required = false) String symbols) {

        log.info("Mock Service 2: Received request for symbols: {}", symbols);

        Map<String, BigDecimal> rates = new HashMap<>();

        if (symbols != null && !symbols.isBlank()) {
            for (String symbol : symbols.split(",")) {
                String normalizedSymbol = symbol.trim().toUpperCase();
                if (BASE_RATES.containsKey(normalizedSymbol)) {
                    rates.put(normalizedSymbol, generateRandomRate(BASE_RATES.get(normalizedSymbol)));
                }
            }
        } else {
            BASE_RATES.forEach((currency, baseRate) ->
                    rates.put(currency, generateRandomRate(baseRate))
            );
        }

        ExchangeRateResponse response = ExchangeRateResponse.builder()
                .success(true)
                .timestamp(Instant.now().getEpochSecond())
                .base(BASE_CURRENCY)
                .date(LocalDate.now())
                .rates(rates)
                .build();

        log.info("Mock Service 2: Returning {} rates", rates.size());

        return ResponseEntity.ok(response);
    }

    private BigDecimal generateRandomRate(BigDecimal baseRate) {
        //±15%
        double variation = (RANDOM.nextDouble() - 0.5) * 0.3;
        return baseRate.multiply(BigDecimal.valueOf(1 + variation))
                .setScale(6, RoundingMode.HALF_UP);
    }
}
