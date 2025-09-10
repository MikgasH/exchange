package com.example.cerpshashkin.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

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
        Currency base,
        LocalDate rateDate,
        Map<Currency, BigDecimal> rates
) {
    /**
     * Creates a successful exchange rate response.
     *
     * @param base the base currency
     * @param rateDate the date for rates validity
     * @param rates the exchange rates map
     * @return successful CurrencyExchangeResponse
     */
    public static CurrencyExchangeResponse success(final Currency base,
                                                   final LocalDate rateDate,
                                                   final Map<Currency, BigDecimal> rates) {
        return new CurrencyExchangeResponse(true, Instant.now(), base,
                rateDate, rates);
    }

    /**
     * Creates a failed exchange rate response.
     *
     * @return failed CurrencyExchangeResponse with empty data
     */
    public static CurrencyExchangeResponse failure() {
        return new CurrencyExchangeResponse(false, Instant.now(), null, null, Map.of());
    }
}
