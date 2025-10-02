package com.example.cerpshashkin.client.impl;

import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.client.ApiProvider;
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

@Component
@Slf4j
public class MockExchangeRateClient implements ExchangeRateClient {

    private static final Currency BASE_CURRENCY = Currency.getInstance("USD");

    private static final Map<Currency, BigDecimal> BASE_RATES = Map.of(
            Currency.getInstance("EUR"), BigDecimal.valueOf(0.85),
            Currency.getInstance("GBP"), BigDecimal.valueOf(0.75),
            Currency.getInstance("JPY"), BigDecimal.valueOf(110.0),
            Currency.getInstance("CAD"), BigDecimal.valueOf(1.25),
            Currency.getInstance("CHF"), BigDecimal.valueOf(0.95),
            Currency.getInstance("AUD"), BigDecimal.valueOf(1.35)
    );

    private static final String LOG_INFO_GENERATING_ALL_RATES = "Mock client generating random exchange rates for all currencies";
    private static final String LOG_INFO_GENERATING_SYMBOLS_RATES = "Mock client generating exchange rates for symbols: {}";
    private static final String LOG_WARN_INVALID_CURRENCY = "Invalid currency code: {}";
    private static final String LOG_DEBUG_GENERATED_RATES = "Generated mock rates: {}";

    private static final String ERROR_SYMBOLS_NULL_OR_EMPTY = "Symbols parameter cannot be null or empty";

    private final Random random = new Random();

    @Override
    public CurrencyExchangeResponse getLatestRates() {
        log.info(LOG_INFO_GENERATING_ALL_RATES);

        return CurrencyExchangeResponse.success(
                BASE_CURRENCY,
                LocalDate.now(),
                generateRandomRates()
        );
    }

    @Override
    public CurrencyExchangeResponse getLatestRates(final String symbols) {
        if (!StringUtils.hasText(symbols)) {
            throw new IllegalArgumentException(ERROR_SYMBOLS_NULL_OR_EMPTY);
        }

        log.info(LOG_INFO_GENERATING_SYMBOLS_RATES, symbols);

        final Map<Currency, BigDecimal> filteredRates = new HashMap<>();

        for (String currencyCode : symbols.split(",")) {
            currencyCode = currencyCode.trim().toUpperCase();
            try {
                final Currency currency = Currency.getInstance(currencyCode);

                if (!currency.equals(BASE_CURRENCY) && BASE_RATES.containsKey(currency)) {
                    filteredRates.put(currency, generateRandomRate(BASE_RATES.get(currency)));
                }
            } catch (IllegalArgumentException e) {
                log.warn(LOG_WARN_INVALID_CURRENCY, currencyCode);
            }
        }

        return CurrencyExchangeResponse.success(
                BASE_CURRENCY,
                LocalDate.now(),
                filteredRates
        );
    }

    @Override
    public String getProviderName() {
        return ApiProvider.MOCK.getDisplayName();
    }

    private Map<Currency, BigDecimal> generateRandomRates() {
        final Map<Currency, BigDecimal> rates = new HashMap<>();

        BASE_RATES.forEach((currency, baseRate) ->
                rates.put(currency, generateRandomRate(baseRate))
        );

        log.debug(LOG_DEBUG_GENERATED_RATES, rates);
        return rates;
    }

    private BigDecimal generateRandomRate(final BigDecimal baseRate) {
        final double variation = (random.nextDouble() - 0.5) * 0.2;
        return baseRate.multiply(BigDecimal.valueOf(1 + variation))
                .setScale(6, RoundingMode.HALF_UP);
    }
}
