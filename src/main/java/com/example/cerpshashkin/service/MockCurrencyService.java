package com.example.cerpshashkin.service;

import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mock implementation of currency exchange service.
 */
@Service
@Slf4j
public class MockCurrencyService {

    /** Exchange rate for Canadian Dollar. */
    private static final BigDecimal CAD_RATE = BigDecimal.valueOf(1.260046);
    /** Exchange rate for Swiss Franc. */
    private static final BigDecimal CHF_RATE = BigDecimal.valueOf(0.933058);
    /** Exchange rate for Euro. */
    private static final BigDecimal EUR_RATE = BigDecimal.valueOf(0.806942);
    /** Exchange rate for British Pound. */
    private static final BigDecimal GBP_RATE = BigDecimal.valueOf(0.719154);
    /** Exchange rate for Japanese Yen. */
    private static final BigDecimal JPY_RATE = BigDecimal.valueOf(107.346001);
    /** Exchange rate for Australian Dollar. */
    private static final BigDecimal AUD_RATE = BigDecimal.valueOf(1.355018);

    /**
     * Gets current exchange rates for supported currencies.
     *
     * @return CurrencyExchangeResponse with current exchange rates
     */
    public CurrencyExchangeResponse getExchangeRates() {
        log.info("Class MockCurrencyService method getExchangeRates");

        final Map<Currency, BigDecimal> rates = new HashMap<>();
        rates.put(Currency.getInstance("CAD"), CAD_RATE);
        rates.put(Currency.getInstance("CHF"), CHF_RATE);
        rates.put(Currency.getInstance("EUR"), EUR_RATE);
        rates.put(Currency.getInstance("GBP"), GBP_RATE);
        rates.put(Currency.getInstance("JPY"), JPY_RATE);
        rates.put(Currency.getInstance("AUD"), AUD_RATE);

        return CurrencyExchangeResponse.success(
                Currency.getInstance("USD"),
                LocalDate.now(),
                rates
        );
    }

    /**
     * Gets list of supported currencies.
     * Using a predefined set of major currencies for now.
     * Later this will be moved to database storage.
     *
     * @return list of supported currency codes
     */
    public List<String> getSupportedCurrencies() {
        log.info("Class MockCurrencyService method getSupportedCurrencies");

        final Set<Currency> supportedCurrencies = Set.of(
                Currency.getInstance("USD"),
                Currency.getInstance("EUR"),
                Currency.getInstance("GBP"),
                Currency.getInstance("JPY"),
                Currency.getInstance("CAD"),
                Currency.getInstance("CHF"),
                Currency.getInstance("AUD")
        );

        return supportedCurrencies.stream()
                .map(Currency::getCurrencyCode)
                .sorted()
                .toList();
    }
}
