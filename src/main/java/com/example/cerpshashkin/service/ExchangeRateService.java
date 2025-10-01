package com.example.cerpshashkin.service;

import com.example.cerpshashkin.exception.ExchangeRateNotAvailableException;
import com.example.cerpshashkin.model.CachedRate;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import com.example.cerpshashkin.service.cache.CurrencyRateCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private static final String LOG_CACHE_HIT_DIRECT = "Cache HIT: {} -> {} (direct)";
    private static final String LOG_CACHE_HIT_INVERSE = "Cache HIT: {} -> {} (inverse)";
    private static final String LOG_CACHE_HIT_CROSS = "Cache HIT: {} -> {} (cross-rate via {})";
    private static final String LOG_CACHE_MISS = "Cache MISS: {} -> {}, fetching from providers";
    private static final String LOG_ERROR_GET_RATES = "Failed to get exchange rates from providers";
    private static final String LOG_ERROR_REFRESH_RATES = "Failed to refresh exchange rates";
    private static final String LOG_INFO_GET_LATEST_RATES = "Getting latest exchange rates";
    private static final String LOG_INFO_GET_LATEST_RATES_SYMBOLS = "Getting latest exchange rates for symbols: {}";
    private static final String LOG_INFO_REFRESHING_RATES = "Refreshing exchange rates";
    private static final String LOG_INFO_RATES_REFRESHED = "Exchange rates refreshed and cached";
    private static final String LOG_INFO_CACHING_RESPONSE = "Caching exchange rates from response";
    private static final String ERROR_REFRESH_RATES_MESSAGE = "Failed to refresh exchange rates: ";
    private static final String UNSUCCESSFUL_RESPONSE = "Provider returned unsuccessful response";
    private static final int SCALE = 6;

    @Value("${exchange-rates.base-currency:EUR}")
    private String baseCurrencyCode;

    private final ExchangeRateProviderService providerService;
    private final CurrencyRateCache cache;

    public Optional<BigDecimal> getExchangeRate(final Currency from, final Currency to) {
        return cache.getRate(from, to)
                .map(cachedRate -> {
                    log.info(LOG_CACHE_HIT_DIRECT, from.getCurrencyCode(), to.getCurrencyCode());
                    return cachedRate.rate();
                })
                .or(() -> calculateInverseFromCache(from, to))
                .or(() -> calculateCrossRateFromCache(from, to))
                .or(() -> {
                    log.warn(LOG_CACHE_MISS, from.getCurrencyCode(), to.getCurrencyCode());
                    return getExchangeRateFromProviders(from, to);
                });
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
                        cache.putRate(response.base(), currency, rate)
                ));
    }

    private Optional<BigDecimal> calculateInverseFromCache(final Currency from, final Currency to) {
        return cache.getRate(to, from)
                .map(reverseCachedRate -> {
                    final BigDecimal inverseRate = BigDecimal.ONE.divide(
                            reverseCachedRate.rate(),
                            SCALE,
                            RoundingMode.HALF_UP
                    );
                    log.info(LOG_CACHE_HIT_INVERSE, from.getCurrencyCode(), to.getCurrencyCode());
                    return inverseRate;
                });
    }

    private Optional<BigDecimal> calculateCrossRateFromCache(final Currency from, final Currency to) {
        final Currency baseCurrency = Currency.getInstance(baseCurrencyCode);

        final Optional<BigDecimal> fromRate = cache.getRate(baseCurrency, from)
                .map(CachedRate::rate);

        final Optional<BigDecimal> toRate = cache.getRate(baseCurrency, to)
                .map(CachedRate::rate);

        if (fromRate.isPresent() && toRate.isPresent()) {
            final BigDecimal crossRate = toRate.get()
                    .divide(fromRate.get(), SCALE, RoundingMode.HALF_UP);

            log.info(LOG_CACHE_HIT_CROSS, from.getCurrencyCode(), to.getCurrencyCode(), baseCurrency.getCurrencyCode());
            return Optional.of(crossRate);
        }

        return Optional.empty();
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
}
