package com.example.cerpshashkin.service;

import com.example.cerpshashkin.exception.ExchangeRateNotAvailableException;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import com.example.cerpshashkin.service.cache.CurrencyRateCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRateService {

    private static final String LOG_DEBUG_GET_RATE = "Getting exchange rate: {} -> {}";
    private static final String LOG_DEBUG_FOUND_CACHED_RATE = "Found cached rate for {} -> {}: {}";
    private static final String LOG_ERROR_GET_RATES = "Failed to get exchange rates from providers";
    private static final String LOG_INFO_GET_LATEST_RATES = "Getting latest exchange rates";
    private static final String LOG_INFO_GET_LATEST_RATES_SYMBOLS = "Getting latest exchange rates for symbols: {}";
    private static final String LOG_INFO_REFRESHING_RATES = "Refreshing exchange rates";
    private static final String LOG_INFO_RATES_REFRESHED = "Exchange rates refreshed and cached";
    private static final String LOG_INFO_CACHING_RESPONSE = "Caching exchange rates from response";
    private static final String ERROR_REFRESH_RATES_MESSAGE = "Failed to refresh exchange rates: ";
    private static final String CACHE_PROVIDER_NAME = "ExchangeRateService";
    private static final int SCALE = 6;
    private static final String LOG_ERROR_REFRESH_RATES = "Failed to refresh exchange rates";
    private static final String UNSUCCESSFUL_RESPONSE = "Provider returned unsuccessful response";

    private final ExchangeRateProviderService providerService;
    private final CurrencyRateCache cache;

    public Optional<BigDecimal> getExchangeRate(final Currency from, final Currency to) {
        log.debug(LOG_DEBUG_GET_RATE, from, to);

        return cache.getRate(from, to)
                .map(cachedRate -> {
                    log.debug(LOG_DEBUG_FOUND_CACHED_RATE, from, to, cachedRate.rate());
                    return cachedRate.rate();
                })
                .or(() -> getExchangeRateFromProviders(from, to));
    }

    private Optional<BigDecimal> getExchangeRateFromProviders(final Currency from, final Currency to) {
        try {
            return Optional.of(providerService.getLatestRatesFromProviders())
                    .filter(CurrencyExchangeResponse::success)
                    .filter(response -> response.rates() != null)
                    .flatMap(response -> {
                        cacheExchangeRates(response);
                        return calculateExchangeRate(from, to, response);
                    });
        } catch (Exception e) {
            log.error(LOG_ERROR_GET_RATES, e);
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> calculateExchangeRate(
            final Currency from,
            final Currency to,
            final CurrencyExchangeResponse response) {

        final Currency base = response.base();
        final Map<Currency, BigDecimal> rates = response.rates();

        if (from.equals(base)) {
            return Optional.ofNullable(rates.get(to));
        }

        if (to.equals(base)) {
            return Optional.ofNullable(rates.get(from))
                    .map(rate -> BigDecimal.ONE.divide(rate, SCALE, RoundingMode.HALF_UP));
        }

        return Optional.ofNullable(rates.get(from))
                .flatMap(fromRate -> Optional.ofNullable(rates.get(to))
                        .map(toRate -> toRate.divide(fromRate, SCALE, RoundingMode.HALF_UP))
                );
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
            } else {
                throw new ExchangeRateNotAvailableException(ERROR_REFRESH_RATES_MESSAGE + UNSUCCESSFUL_RESPONSE);
            }
        } catch (ExchangeRateNotAvailableException e) {
            log.error(LOG_ERROR_REFRESH_RATES, e);
            throw e;
        } catch (Exception e) {
            log.error(LOG_ERROR_REFRESH_RATES, e);
            throw new ExchangeRateNotAvailableException(ERROR_REFRESH_RATES_MESSAGE + e.getMessage());
        }
    }
    public void cacheExchangeRates(final CurrencyExchangeResponse response) {
        log.info(LOG_INFO_CACHING_RESPONSE);
        Optional.ofNullable(response.rates())
                .ifPresent(rates -> rates.forEach((currency, rate) ->
                        cache.putRate(response.base(), currency, rate, CACHE_PROVIDER_NAME)
                ));
    }
}
