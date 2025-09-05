package com.example.cerpshashkin.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

public record CurrencyExchangeResponse(
        boolean success,
        Instant lastUpdated,
        Currency base,
        LocalDate rateDate,
        Map<Currency, BigDecimal> rates
) {
    public static CurrencyExchangeResponse success(Currency base, LocalDate rateDate, Map<Currency, BigDecimal> rates) {
        return new CurrencyExchangeResponse(true, Instant.now(), base, rateDate, rates);
    }
}
