package com.example.cerpshashkin.service;

import com.example.cerpshashkin.exception.ExchangeRateNotAvailableException;
import com.example.cerpshashkin.model.CachedRate;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import com.example.cerpshashkin.service.cache.CurrencyRateCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceUnitTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final Currency JPY = Currency.getInstance("JPY");
    private static final LocalDate TEST_DATE = LocalDate.of(2025, 10, 1);

    @Mock
    private ExchangeRateProviderService providerService;

    @Mock
    private CurrencyRateCache cache;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exchangeRateService, "baseCurrencyCode", "EUR");
    }

    @Test
    void getExchangeRate_WithCachedRate_ShouldReturnCachedValue() {
        BigDecimal cachedRate = BigDecimal.valueOf(0.847);
        CachedRate cachedRateObj = new CachedRate(cachedRate, Instant.now());

        when(cache.getRate(USD, EUR)).thenReturn(Optional.of(cachedRateObj));

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(USD, EUR);

        assertThat(result).contains(cachedRate);
        verify(cache).getRate(USD, EUR);
        verifyNoInteractions(providerService);
    }

    @Test
    void getExchangeRate_WithoutCachedRate_ShouldCallProviderAndCache() {
        BigDecimal exchangeRate = BigDecimal.valueOf(1.18);

        when(cache.getRate(EUR, USD)).thenReturn(Optional.empty());

        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                EUR, TEST_DATE, Map.of(USD, exchangeRate)
        );
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(EUR, USD);

        assertThat(result).contains(exchangeRate);
        verify(cache, atLeastOnce()).getRate(EUR, USD);
        verify(providerService).getLatestRatesFromProviders();
        verify(cache).putRate(EUR, USD, exchangeRate);
    }

    @Test
    void getExchangeRate_WithProviderFailure_ShouldReturnEmpty() {
        when(cache.getRate(USD, EUR)).thenReturn(Optional.empty());
        when(cache.getRate(EUR, USD)).thenReturn(Optional.empty());
        when(cache.getRate(EUR, USD)).thenReturn(Optional.empty());
        when(cache.getRate(EUR, EUR)).thenReturn(Optional.empty());
        when(providerService.getLatestRatesFromProviders()).thenThrow(new RuntimeException("Provider failed"));

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(USD, EUR);

        assertThat(result).isEmpty();
        verify(cache).getRate(USD, EUR);
        verify(providerService).getLatestRatesFromProviders();
        verify(cache, never()).putRate(any(), any(), any());
    }

    @Test
    void getExchangeRate_DirectConversion_ShouldReturnDirectRate() {
        BigDecimal directRate = BigDecimal.valueOf(1.18);

        when(cache.getRate(EUR, USD)).thenReturn(Optional.empty());

        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                EUR, TEST_DATE, Map.of(USD, directRate)
        );
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(EUR, USD);

        assertThat(result).contains(directRate);
    }

    @Test
    void getExchangeRate_InverseConversion_ShouldReturnInverseRate() {
        BigDecimal eurToUsdRate = BigDecimal.valueOf(1.18);

        when(cache.getRate(USD, EUR)).thenReturn(Optional.empty());
        when(cache.getRate(EUR, USD)).thenReturn(Optional.empty());

        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                EUR, TEST_DATE, Map.of(USD, eurToUsdRate)
        );
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(USD, EUR);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo(
                BigDecimal.ONE.divide(eurToUsdRate, 6, RoundingMode.HALF_UP)
        );
    }

    @Test
    void getExchangeRate_CrossConversion_ShouldReturnCrossRate() {
        BigDecimal eurToUsd = BigDecimal.valueOf(1.18);
        BigDecimal eurToGbp = BigDecimal.valueOf(0.87);

        when(cache.getRate(USD, GBP)).thenReturn(Optional.empty());
        when(cache.getRate(GBP, USD)).thenReturn(Optional.empty());

        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                EUR, TEST_DATE, Map.of(USD, eurToUsd, GBP, eurToGbp)
        );
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(USD, GBP);

        BigDecimal expectedCrossRate = eurToGbp.divide(eurToUsd, 6, RoundingMode.HALF_UP);
        assertThat(result).contains(expectedCrossRate);
    }

    @Test
    void getExchangeRate_WithMissingRates_ShouldReturnEmpty() {
        when(cache.getRate(USD, JPY)).thenReturn(Optional.empty());
        when(cache.getRate(JPY, USD)).thenReturn(Optional.empty());
        when(cache.getRate(EUR, USD)).thenReturn(Optional.empty());
        when(cache.getRate(EUR, JPY)).thenReturn(Optional.empty());

        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                EUR, TEST_DATE, Map.of(GBP, BigDecimal.valueOf(0.87))
        );
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(USD, JPY);

        assertThat(result).isEmpty();
    }

    @Test
    void getLatestRates_ShouldDelegateToProviderService() {
        CurrencyExchangeResponse expectedResponse = CurrencyExchangeResponse.success(
                EUR, TEST_DATE, Map.of()
        );
        when(providerService.getLatestRatesFromProviders()).thenReturn(expectedResponse);

        CurrencyExchangeResponse result = exchangeRateService.getLatestRates();

        assertThat(result).isEqualTo(expectedResponse);
        verify(providerService).getLatestRatesFromProviders();
    }

    @Test
    void getLatestRates_WithSymbols_ShouldDelegateToProviderService() {
        String symbols = "EUR,GBP";
        CurrencyExchangeResponse expectedResponse = CurrencyExchangeResponse.success(
                EUR, TEST_DATE, Map.of()
        );
        when(providerService.getLatestRatesFromProviders(symbols)).thenReturn(expectedResponse);

        CurrencyExchangeResponse result = exchangeRateService.getLatestRates(symbols);

        assertThat(result).isEqualTo(expectedResponse);
        verify(providerService).getLatestRatesFromProviders(symbols);
    }

    @Test
    void refreshRates_ShouldClearCacheAndFetchNewRates() {
        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                EUR, TEST_DATE, Map.of(USD, BigDecimal.valueOf(1.18))
        );
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        exchangeRateService.refreshRates();

        verify(cache).clearCache();
        verify(providerService).getLatestRatesFromProviders();
        verify(cache).putRate(EUR, USD, BigDecimal.valueOf(1.18));
    }

    @Test
    void refreshRates_WithUnsuccessfulResponse_ShouldThrowExceptionAndNotCache() {
        CurrencyExchangeResponse response = CurrencyExchangeResponse.failure();
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        assertThatThrownBy(() -> exchangeRateService.refreshRates())
                .isInstanceOf(ExchangeRateNotAvailableException.class)
                .hasMessageContaining("Provider returned unsuccessful response");

        verify(cache).clearCache();
        verify(cache, never()).putRate(any(), any(), any());
    }
}
