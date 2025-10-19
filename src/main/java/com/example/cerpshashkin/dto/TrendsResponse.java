package com.example.cerpshashkin.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TrendsResponse(
        boolean success,
        String fromCurrency,
        String toCurrency,
        String period,
        BigDecimal oldestRate,
        BigDecimal newestRate,
        BigDecimal changePercentage,
        Instant oldestRateTimestamp,
        Instant newestRateTimestamp,
        int dataPointsCount
) {
    public static TrendsResponse success(
            final String from,
            final String to,
            final String period,
            final BigDecimal oldestRate,
            final BigDecimal newestRate,
            final BigDecimal changePercentage,
            final Instant oldestTimestamp,
            final Instant newestTimestamp,
            final int dataPoints
    ) {
        return new TrendsResponse(
                true,
                from,
                to,
                period,
                oldestRate,
                newestRate,
                changePercentage,
                oldestTimestamp,
                newestTimestamp,
                dataPoints
        );
    }
}
