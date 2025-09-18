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

@Service
@Slf4j
public class MockCurrencyService {

    private static final BigDecimal CAD_RATE = BigDecimal.valueOf(1.260046);
    private static final BigDecimal CHF_RATE = BigDecimal.valueOf(0.933058);
    private static final BigDecimal EUR_RATE = BigDecimal.valueOf(0.806942);
    private static final BigDecimal GBP_RATE = BigDecimal.valueOf(0.719154);
    private static final BigDecimal JPY_RATE = BigDecimal.valueOf(107.346001);
    private static final BigDecimal AUD_RATE = BigDecimal.valueOf(1.355018);

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
