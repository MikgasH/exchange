package com.example.cerpshashkin.service;

import com.example.cerpshashkin.exception.ExchangeRateNotAvailableException;
import com.example.cerpshashkin.model.CachedRate;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import com.example.cerpshashkin.service.cache.CurrencyRateCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateProviderService providerService;
    private final CurrencyRateCache cache;

    private static final String LOG_DEBUG_GET_RATE = "Getting exchange rate: {} -> {}";
    private static final String LOG_DEBUG_FOUND_CACHED_RATE = "Found cached rate for {} -> {}: {}";
    private static final String LOG_ERROR_GET_RATES = "Failed to get exchange rates from providers";
    private static final String LOG_DEBUG_DIRECT_CONVERSION = "Direct conversion {} -> {}: {}";
    private static final String LOG_DEBUG_INVERSE_CONVERSION = "Inverse conversion {} -> {}: {}";
    private static final String LOG_DEBUG_CROSS_CONVERSION = "Cross conversion {} -> {}: {}";
    private static final String LOG_INFO_GET_LATEST_RATES = "Getting latest exchange rates";
    private static final String LOG_INFO_GET_LATEST_RATES_SYMBOLS = "Getting latest exchange rates for symbols: {}";
    private static final String LOG_INFO_REFRESHING_RATES = "Refreshing exchange rates";
    private static final String LOG_INFO_RATES_REFRESHED = "Exchange rates refreshed and cached";
    private static final String LOG_ERROR_REFRESH_RATES = "Failed to refresh exchange rates";
    private static final String ERROR_REFRESH_RATES_MESSAGE = "Failed to refresh exchange rates: ";
    private static final String CACHE_PROVIDER_NAME = "ExchangeRateService";

    public Optional<BigDecimal> getExchangeRate(final Currency from, final Currency to) {
        log.debug(LOG_DEBUG_GET_RATE, from, to);

        final Optional<BigDecimal> cachedRate = getCachedRate(from, to);
        if (cachedRate.isPresent()) {
            return cachedRate;
        }

        return getExchangeRateFromProviders(from, to);
    }

    private Optional<BigDecimal> getCachedRate(final Currency from, final Currency to) {
        final Optional<CachedRate> cachedRate = cache.getRate(from, to);
        if (cachedRate.isPresent()) {
            log.debug(LOG_DEBUG_FOUND_CACHED_RATE, from, to, cachedRate.get().rate());
            return Optional.of(cachedRate.get().rate());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> getExchangeRateFromProviders(final Currency from, final Currency to) {
        try {
            final CurrencyExchangeResponse response = providerService.getLatestRatesFromProviders();
            if (response.success() && response.rates() != null) {
                cacheExchangeRates(response);
                return calculateExchangeRate(from, to, response);
            }
        } catch (Exception e) {
            log.error(LOG_ERROR_GET_RATES, e);
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> calculateExchangeRate(final Currency from, final Currency to,
                                                       final CurrencyExchangeResponse response) {
        final Currency baseCurrency = response.base();

        if (from.equals(baseCurrency)) {
            return getDirectConversionRate(from, to, response);
        } else if (to.equals(baseCurrency)) {
            return getInverseConversionRate(from, to, response);
        } else {
            return getCrossConversionRate(from, to, response);
        }
    }

    private Optional<BigDecimal> getDirectConversionRate(final Currency from, final Currency to,
                                                         final CurrencyExchangeResponse response) {
        final BigDecimal rate = response.rates().get(to);
        if (rate != null) {
            log.debug(LOG_DEBUG_DIRECT_CONVERSION, from, to, rate);
            return Optional.of(rate);
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> getInverseConversionRate(final Currency from, final Currency to,
                                                          final CurrencyExchangeResponse response) {
        final BigDecimal rate = response.rates().get(from);
        if (rate != null) {
            final BigDecimal inverseRate = BigDecimal.ONE.divide(rate, 6, java.math.RoundingMode.HALF_UP);
            log.debug(LOG_DEBUG_INVERSE_CONVERSION, from, to, inverseRate);
            return Optional.of(inverseRate);
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> getCrossConversionRate(final Currency from, final Currency to,
                                                        final CurrencyExchangeResponse response) {
        final BigDecimal fromRate = response.rates().get(from);
        final BigDecimal toRate = response.rates().get(to);

        if (fromRate != null && toRate != null) {
            final BigDecimal crossRate = toRate.divide(fromRate, 6, java.math.RoundingMode.HALF_UP);
            log.debug(LOG_DEBUG_CROSS_CONVERSION, from, to, crossRate);
            return Optional.of(crossRate);
        }
        return Optional.empty();
    }

    public CurrencyExchangeResponse getLatestRates() {
        log.info(LOG_INFO_GET_LATEST_RATES);
        return providerService.getLatestRatesFromProviders();
    }

    public CurrencyExchangeResponse getLatestRates(final String symbols) {
        log.info(LOG_INFO_GET_LATEST_RATES_SYMBOLS, symbols);
        return providerService.getLatestRatesFromProviders(symbols);
    }

    public void refreshRates() {
        log.info(LOG_INFO_REFRESHING_RATES);
        cache.clearCache();
        try {
            final CurrencyExchangeResponse response = providerService.getLatestRatesFromProviders();
            if (response.success()) {
                cacheExchangeRates(response);
                log.info(LOG_INFO_RATES_REFRESHED);
            }
        } catch (Exception e) {
            log.error(LOG_ERROR_REFRESH_RATES, e);
            throw new ExchangeRateNotAvailableException(ERROR_REFRESH_RATES_MESSAGE + e.getMessage());
        }
    }

    private void cacheExchangeRates(final CurrencyExchangeResponse response) {
        if (response.rates() != null) {
            response.rates().forEach((currency, rate) ->
                    cache.putRate(response.base(), currency, rate, CACHE_PROVIDER_NAME)
            );
        }
    }
}
