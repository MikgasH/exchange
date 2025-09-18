package com.example.cerpshashkin.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ConversionResponse(
        boolean success,
        BigDecimal originalAmount,
        String fromCurrency,
        String toCurrency,
        BigDecimal convertedAmount,
        BigDecimal exchangeRate,
        Instant timestamp,
        String provider
) {
    public static ConversionResponse success(final BigDecimal originalAmount, final String from, final String to,
                                             final BigDecimal convertedAmount, final BigDecimal rate, final String provider) {
        return new ConversionResponse(
                true, originalAmount, from, to, convertedAmount, rate, Instant.now(), provider
        );
    }

    public static ConversionResponse failure(final BigDecimal originalAmount, final String from, final String to) {
        return new ConversionResponse(
                false, originalAmount, from, to, null, null, Instant.now(), null
        );
    }
}
