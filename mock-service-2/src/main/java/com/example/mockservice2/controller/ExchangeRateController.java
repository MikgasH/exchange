package com.example.mockservice2.controller;

import com.example.mockservice2.dto.ExchangeRateResponse;
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
    private static final String LOG_GENERATING_ALL = "Mock Service 2: Generating rates for all available currencies";
    private static final String LOG_RETURNING_RATES = "Mock Service 2: Returning {} rates";
    private static final int SCALE = 6;

    private static final Map<String, BigDecimal> BASE_RATES = Map.ofEntries(
            Map.entry("USD", BigDecimal.valueOf(1.08)),
            Map.entry("GBP", BigDecimal.valueOf(0.85)),
            Map.entry("JPY", BigDecimal.valueOf(160.0)),
            Map.entry("CHF", BigDecimal.valueOf(0.97)),
            Map.entry("CAD", BigDecimal.valueOf(1.5)),
            Map.entry("AUD", BigDecimal.valueOf(1.65)),
            Map.entry("CNY", BigDecimal.valueOf(7.8)),
            Map.entry("SEK", BigDecimal.valueOf(11.5)),
            Map.entry("NZD", BigDecimal.valueOf(1.7)),
            Map.entry("BYN", BigDecimal.valueOf(3.5)),
            Map.entry("NOK", BigDecimal.valueOf(11.8)),
            Map.entry("DKK", BigDecimal.valueOf(7.45)),
            Map.entry("PLN", BigDecimal.valueOf(4.3)),
            Map.entry("CZK", BigDecimal.valueOf(25.0)),
            Map.entry("HUF", BigDecimal.valueOf(390.0)),
            Map.entry("RON", BigDecimal.valueOf(4.95)),
            Map.entry("TRY", BigDecimal.valueOf(35.0)),
            Map.entry("INR", BigDecimal.valueOf(90.0)),
            Map.entry("BRL", BigDecimal.valueOf(5.4)),
            Map.entry("MXN", BigDecimal.valueOf(18.5))
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
        //±15%
        double variation = (RANDOM.nextDouble() - 0.5) * 0.3;
        return baseRate.multiply(BigDecimal.valueOf(1 + variation))
                .setScale(SCALE, RoundingMode.HALF_UP);
    }
}
