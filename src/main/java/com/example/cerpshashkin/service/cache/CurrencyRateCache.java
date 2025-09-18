package com.example.cerpshashkin.service.cache;

import com.example.cerpshashkin.model.CachedRate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class CurrencyRateCache {

    private final Map<String, CachedRate> cache = new ConcurrentHashMap<>();

    @Value("${cache.exchange-rates.ttl:3600}")
    private long cacheTtlSeconds;

    private static final String LOG_DEBUG_PUT_RATE = "Cached rate {} -> {}: {} from {}";
    private static final String LOG_DEBUG_REMOVED_EXPIRED_RATE = "Removed expired rate for {} -> {}";
    private static final String LOG_DEBUG_RETRIEVED_RATE = "Retrieved cached rate {} -> {}: {}";
    private static final String LOG_INFO_CACHE_CLEARED = "Currency rate cache cleared";
    private static final String KEY_SEPARATOR = "_";

    public void putRate(final Currency from, final Currency to, final BigDecimal rate, final String provider) {
        final String key = createKey(from, to);
        final CachedRate cachedRate = new CachedRate(rate, provider, Instant.now());
        cache.put(key, cachedRate);
        log.debug(LOG_DEBUG_PUT_RATE, from, to, rate, provider);
    }

    public Optional<CachedRate> getRate(final Currency from, final Currency to) {
        final String key = createKey(from, to);
        final CachedRate cachedRate = cache.get(key);

        if (cachedRate == null) {
            return Optional.empty();
        }

        if (isExpired(cachedRate)) {
            cache.remove(key);
            log.debug(LOG_DEBUG_REMOVED_EXPIRED_RATE, from, to);
            return Optional.empty();
        }

        log.debug(LOG_DEBUG_RETRIEVED_RATE, from, to, cachedRate.rate());
        return Optional.of(cachedRate);
    }

    public void clearCache() {
        cache.clear();
        log.info(LOG_INFO_CACHE_CLEARED);
    }

    private String createKey(final Currency from, final Currency to) {
        return from.getCurrencyCode() + KEY_SEPARATOR + to.getCurrencyCode();
    }

    private boolean isExpired(final CachedRate cachedRate) {
        return cachedRate.timestamp().plusSeconds(cacheTtlSeconds).isBefore(Instant.now());
    }
}
