package com.example.cerpshashkin.client.impl;

import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Mock exchange rate client that generates random rates for testing and fallback purposes.
 * Can be used when external APIs are unavailable or API tokens are exhausted.
 */
@Component
@Slf4j
public class MockExchangeRateClient implements ExchangeRateClient {

    private static final String PROVIDER_NAME = "MockAPI";

    private final Random random = new Random();

    private static final Map<Currency, BigDecimal> BASE_RATES = Map.of(
            Currency.getInstance("USD"), BigDecimal.valueOf(1.0),
            Currency.getInstance("EUR"), BigDecimal.valueOf(0.85),
            Currency.getInstance("GBP"), BigDecimal.valueOf(0.75),
            Currency.getInstance("JPY"), BigDecimal.valueOf(110.0),
            Currency.getInstance("CAD"), BigDecimal.valueOf(1.25),
            Currency.getInstance("CHF"), BigDecimal.valueOf(0.95),
            Currency.getInstance("AUD"), BigDecimal.valueOf(1.35)
    );

    @Override
    public CurrencyExchangeResponse getLatestRates() {
        log.info("Mock client generating random exchange rates for all currencies");

        return CurrencyExchangeResponse.success(
                Currency.getInstance("EUR"),
                LocalDate.now(),
                generateRandomRates()
        );
    }

    @Override
    public CurrencyExchangeResponse getLatestRates(final String symbols) {
        if (!StringUtils.hasText(symbols)) {
            throw new IllegalArgumentException("Symbols parameter cannot be null or empty");
        }

        log.info("Mock client generating exchange rates for symbols: {}", symbols);

        final Map<Currency, BigDecimal> filteredRates = new HashMap<>();

        for (String currencyCode : symbols.split(",")) {
            currencyCode = currencyCode.trim();
            try {
                final Currency currency = Currency.getInstance(currencyCode);
                if (BASE_RATES.containsKey(currency)) {
                    filteredRates.put(currency, generateRandomRate(BASE_RATES.get(currency)));
                } else {
                    log.debug("Currency {} not in base rates, skipping", currencyCode);
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid currency code: {}", currencyCode);
            }
        }

        return CurrencyExchangeResponse.success(
                Currency.getInstance("EUR"),
                LocalDate.now(),
                filteredRates
        );
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * Generates random exchange rates for all supported currencies except EUR (base currency).
     *
     * @return map of currencies to randomized exchange rates
     */
    private Map<Currency, BigDecimal> generateRandomRates() {
        final Map<Currency, BigDecimal> rates = new HashMap<>();
        BASE_RATES.entrySet().stream()
                .filter(entry -> !Currency.getInstance("EUR").equals(entry.getKey()))
                .forEach(entry -> rates.put(entry.getKey(), generateRandomRate(entry.getValue())));
        return rates;
    }

    /**
     * Generates a random exchange rate based on a base rate with ±20% variation.
     *
     * @param baseRate the base rate to vary
     * @return randomized exchange rate
     */
    private BigDecimal generateRandomRate(final BigDecimal baseRate) {
        final double variation = (random.nextDouble() - 0.5) * 0.2; // ±10% variation
        return baseRate.multiply(BigDecimal.valueOf(1 + variation))
                .setScale(6, RoundingMode.HALF_UP);
    }
}
