package com.example.cerpshashkin.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CurrencyExchangeResponse(
        boolean success,
        Instant lastUpdated,
        CurrencyEnum base,
        LocalDate rateDate,
        List<Rate> rates
) {
    public static CurrencyExchangeResponse success(CurrencyEnum base, LocalDate rateDate, List<Rate> rates) {
        return new CurrencyExchangeResponse(true, Instant.now(), base, rateDate, rates);
    }
}