package com.example.cerpshashkin.client;

import com.example.cerpshashkin.model.CurrencyExchangeResponse;

/**
 * Common interface for external exchange rate providers.
 * All exchange rate clients should implement this interface to ensure consistency.
 */
public interface ExchangeRateClient {

    /**
     * Retrieves the latest exchange rates for all available currencies.
     *
     * @return CurrencyExchangeResponse containing latest exchange rates
     * @throws RuntimeException if the request fails
     */
    CurrencyExchangeResponse getLatestRates();

    /**
     * Retrieves the latest exchange rates for specified currencies.
     *
     * @param symbols comma-separated list of currency codes (e.g., "EUR,GBP,JPY")
     * @return CurrencyExchangeResponse containing exchange rates for specified currencies
     * @throws RuntimeException if the request fails
     * @throws IllegalArgumentException if symbols parameter is null or empty
     */
    CurrencyExchangeResponse getLatestRates(String symbols);

    /**
     * Returns the name of the exchange rate provider.
     *
     * @return provider name for logging and error handling
     */
    String getProviderName();
}
