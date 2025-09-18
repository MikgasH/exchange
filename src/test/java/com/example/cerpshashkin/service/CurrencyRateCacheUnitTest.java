package com.example.cerpshashkin.service;

import com.example.cerpshashkin.model.CachedRate;
import com.example.cerpshashkin.service.cache.CurrencyRateCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyRateCacheUnitTest {

    private CurrencyRateCache cache;
    private Currency usd;
    private Currency eur;

    @BeforeEach
    void setUp() {
        cache = new CurrencyRateCache();
        ReflectionTestUtils.setField(cache, "cacheTtlSeconds", 3600L);
        usd = Currency.getInstance("USD");
        eur = Currency.getInstance("EUR");
    }

    @Test
    void putRate_ShouldStoreRateSuccessfully() {
        BigDecimal rate = BigDecimal.valueOf(0.85);
        String provider = "TestProvider";

        cache.putRate(usd, eur, rate, provider);

        Optional<CachedRate> result = cache.getRate(usd, eur);
        assertThat(result).isPresent();
        assertThat(result.get().rate()).isEqualTo(rate);
        assertThat(result.get().provider()).isEqualTo(provider);
        assertThat(result.get().timestamp()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void getRate_WithNonExistentPair_ShouldReturnEmpty() {
        Currency gbp = Currency.getInstance("GBP");

        Optional<CachedRate> result = cache.getRate(usd, gbp);

        assertThat(result).isEmpty();
    }

    @Test
    void getRate_WithValidRate_ShouldReturnCachedRate() {
        BigDecimal rate = BigDecimal.valueOf(0.85);
        String provider = "TestProvider";
        cache.putRate(usd, eur, rate, provider);

        Optional<CachedRate> result = cache.getRate(usd, eur);

        assertThat(result).isPresent();
        assertThat(result.get().rate()).isEqualTo(rate);
        assertThat(result.get().provider()).isEqualTo(provider);
    }

    @Test
    void getRate_WithExpiredRate_ShouldReturnEmpty() {
        ReflectionTestUtils.setField(cache, "cacheTtlSeconds", -1L);

        BigDecimal rate = BigDecimal.valueOf(0.85);
        String provider = "TestProvider";
        cache.putRate(usd, eur, rate, provider);

        Optional<CachedRate> result = cache.getRate(usd, eur);

        assertThat(result).isEmpty();
    }

    @Test
    void clearCache_ShouldRemoveAllRates() {
        cache.putRate(usd, eur, BigDecimal.valueOf(0.85), "Provider1");
        cache.putRate(eur, usd, BigDecimal.valueOf(1.18), "Provider2");

        assertThat(cache.getRate(usd, eur)).isPresent();
        assertThat(cache.getRate(eur, usd)).isPresent();

        cache.clearCache();

        assertThat(cache.getRate(usd, eur)).isEmpty();
        assertThat(cache.getRate(eur, usd)).isEmpty();
    }

    @Test
    void putRate_WithDifferentPairs_ShouldStoreSeparately() {
        BigDecimal usdToEur = BigDecimal.valueOf(0.85);
        BigDecimal eurToUsd = BigDecimal.valueOf(1.18);
        String provider = "TestProvider";

        cache.putRate(usd, eur, usdToEur, provider);
        cache.putRate(eur, usd, eurToUsd, provider);

        Optional<CachedRate> result1 = cache.getRate(usd, eur);
        Optional<CachedRate> result2 = cache.getRate(eur, usd);

        assertThat(result1).isPresent();
        assertThat(result1.get().rate()).isEqualTo(usdToEur);
        assertThat(result2).isPresent();
        assertThat(result2.get().rate()).isEqualTo(eurToUsd);
    }

    @Test
    void putRate_WithSamePairTwice_ShouldOverwritePrevious() {
        String provider = "TestProvider";
        BigDecimal oldRate = BigDecimal.valueOf(0.85);
        BigDecimal newRate = BigDecimal.valueOf(0.87);

        cache.putRate(usd, eur, oldRate, provider);
        cache.putRate(usd, eur, newRate, provider);

        Optional<CachedRate> result = cache.getRate(usd, eur);

        assertThat(result).isPresent();
        assertThat(result.get().rate()).isEqualTo(newRate);
    }

    @Test
    void getRate_WithMultipleCurrencyPairs_ShouldReturnCorrectRates() {
        Currency gbp = Currency.getInstance("GBP");
        Currency jpy = Currency.getInstance("JPY");

        cache.putRate(usd, eur, BigDecimal.valueOf(0.85), "Provider1");
        cache.putRate(usd, gbp, BigDecimal.valueOf(0.75), "Provider2");
        cache.putRate(usd, jpy, BigDecimal.valueOf(110.0), "Provider3");

        assertThat(cache.getRate(usd, eur).get().rate()).isEqualTo(BigDecimal.valueOf(0.85));
        assertThat(cache.getRate(usd, gbp).get().rate()).isEqualTo(BigDecimal.valueOf(0.75));
        assertThat(cache.getRate(usd, jpy).get().rate()).isEqualTo(BigDecimal.valueOf(110.0));
    }

    @Test
    void cache_WithNegativeTtl_ShouldExpireImmediately() {
        ReflectionTestUtils.setField(cache, "cacheTtlSeconds", -1L);

        cache.putRate(usd, eur, BigDecimal.valueOf(0.85), "TestProvider");

        assertThat(cache.getRate(usd, eur)).isEmpty();
    }
}
