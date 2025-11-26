package com.example.mockservice1.controller;

import com.example.mockservice1.dto.ExchangeRateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private static final String LOG_GENERATING_ALL = "Mock Service 1: Generating rates for all available currencies";
    private static final String LOG_RETURNING_RATES = "Mock Service 1: Returning {} rates";
    private static final int SCALE = 6;

    private static final Map<String, BigDecimal> BASE_RATES = Map.ofEntries(
            Map.entry("USD", BigDecimal.valueOf(1.1583)),
            Map.entry("GBP", BigDecimal.valueOf(0.8827)),
            Map.entry("JPY", BigDecimal.valueOf(180.85)),
            Map.entry("CHF", BigDecimal.valueOf(0.9284)),
            Map.entry("CAD", BigDecimal.valueOf(1.6230)),
            Map.entry("AUD", BigDecimal.valueOf(1.7853)),
            Map.entry("CNY", BigDecimal.valueOf(8.2359)),
            Map.entry("SEK", BigDecimal.valueOf(10.9920)),
            Map.entry("NZD", BigDecimal.valueOf(2.0585)),
            Map.entry("BYN", BigDecimal.valueOf(3.96)),
            Map.entry("NOK", BigDecimal.valueOf(11.7480)),
            Map.entry("DKK", BigDecimal.valueOf(7.4691)),
            Map.entry("PLN", BigDecimal.valueOf(4.2283)),
            Map.entry("CZK", BigDecimal.valueOf(24.159)),
            Map.entry("HUF", BigDecimal.valueOf(382.33)),
            Map.entry("RON", BigDecimal.valueOf(5.0879)),
            Map.entry("TRY", BigDecimal.valueOf(49.0618)),
            Map.entry("INR", BigDecimal.valueOf(102.5145)),
            Map.entry("BRL", BigDecimal.valueOf(6.1800)),
            Map.entry("MXN", BigDecimal.valueOf(21.2283))
    );

    @GetMapping("/latest")
    public ResponseEntity<ExchangeRateResponse> getLatestRates() {
        log.info(LOG_GENERATING_ALL);

        Map<String, BigDecimal> rates = new HashMap<>();

        BASE_RATES.forEach((currency, baseRate) ->
                rates.put(currency, generateRandomRate(baseRate))
        );

        ExchangeRateResponse response = ExchangeRateResponse.builder()
                .success(true)
                .timestamp(Instant.now().getEpochSecond())
                .base(BASE_CURRENCY)
                .date(LocalDate.now())
                .rates(rates)
                .build();

        log.info(LOG_RETURNING_RATES, rates.size());

        return ResponseEntity.ok(response);
    }

    private BigDecimal generateRandomRate(BigDecimal baseRate) {
        //±10%
        double variation = (RANDOM.nextDouble() - 0.5) * 0.2;
        return baseRate.multiply(BigDecimal.valueOf(1 + variation))
                .setScale(SCALE, RoundingMode.HALF_UP);
    }
}
