package com.example.cerpshashkin.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumMap;

/**
 * Response containing currency exchange rates and metadata.
 * Represents exchange rates relative to a base currency.
 *
 * @param success indicates if the request was successful
 * @param lastUpdated timestamp when rates were last retrieved
 * @param base base currency for exchange rates
 * @param rateDate date for which rates are valid
 * @param rates map of currency codes to exchange rates
 */
public record CurrencyExchangeResponse(
        boolean success,
        Instant lastUpdated,
        CurrencyEnum base,
        LocalDate rateDate,
        EnumMap<CurrencyEnum, BigDecimal> rates
) {
    /**
     * Creates a successful exchange rate response.
     */
    public static CurrencyExchangeResponse success(final CurrencyEnum base,
                                                   final LocalDate rateDate,
                                                   final EnumMap<CurrencyEnum,
                                                           BigDecimal> rates) {
        return new CurrencyExchangeResponse(true, Instant.now(), base,
                rateDate, rates);
    }
}
