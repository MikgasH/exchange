package com.example.cerpshashkin.service;

import com.example.cerpshashkin.exception.ExchangeRateNotAvailableException;
import com.example.cerpshashkin.model.CachedRate;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import com.example.cerpshashkin.service.cache.CurrencyRateCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceUnitTest {

    @Mock
    private ExchangeRateProviderService providerService;

    @Mock
    private CurrencyRateCache cache;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    void getExchangeRate_WithCachedRate_ShouldReturnCachedValue() {
        Currency from = Currency.getInstance("USD");
        Currency to = Currency.getInstance("EUR");
        BigDecimal cachedRate = BigDecimal.valueOf(0.85);
        CachedRate cachedRateObj = new CachedRate(cachedRate, "provider", Instant.now());

        when(cache.getRate(from, to)).thenReturn(Optional.of(cachedRateObj));

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(from, to);

        assertThat(result).contains(cachedRate);
        verify(cache).getRate(from, to);
        verifyNoInteractions(providerService);
    }

    @Test
    void getExchangeRate_WithoutCachedRate_ShouldCallProviderAndCache() {
        Currency from = Currency.getInstance("USD");
        Currency to = Currency.getInstance("EUR");
        Currency base = Currency.getInstance("USD");
        BigDecimal exchangeRate = BigDecimal.valueOf(0.85);

        when(cache.getRate(from, to)).thenReturn(Optional.empty());

        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                base, LocalDate.now(), Map.of(to, exchangeRate)
        );
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(from, to);

        assertThat(result).contains(exchangeRate);
        verify(cache).getRate(from, to);
        verify(providerService).getLatestRatesFromProviders();
        verify(cache).putRate(eq(base), eq(to), eq(exchangeRate), any(String.class));
    }

    @Test
    void getExchangeRate_WithProviderFailure_ShouldReturnEmpty() {
        Currency from = Currency.getInstance("USD");
        Currency to = Currency.getInstance("EUR");

        when(cache.getRate(from, to)).thenReturn(Optional.empty());
        when(providerService.getLatestRatesFromProviders()).thenThrow(new RuntimeException("Provider failed"));

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(from, to);

        assertThat(result).isEmpty();
        verify(cache).getRate(from, to);
        verify(providerService).getLatestRatesFromProviders();
        verify(cache, never()).putRate(any(), any(), any(), any());
    }

    @Test
    void getExchangeRate_DirectConversion_ShouldReturnDirectRate() {
        Currency from = Currency.getInstance("USD");
        Currency to = Currency.getInstance("EUR");
        Currency base = Currency.getInstance("USD");
        BigDecimal directRate = BigDecimal.valueOf(0.85);

        when(cache.getRate(from, to)).thenReturn(Optional.empty());

        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                base, LocalDate.now(), Map.of(to, directRate)
        );
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(from, to);

        assertThat(result).contains(directRate);
    }

    @Test
    void getExchangeRate_InverseConversion_ShouldReturnInverseRate() {
        Currency from = Currency.getInstance("EUR");
        Currency to = Currency.getInstance("USD");
        Currency base = Currency.getInstance("USD");
        BigDecimal eurToUsdRate = BigDecimal.valueOf(1.18);

        when(cache.getRate(from, to)).thenReturn(Optional.empty());

        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                base, LocalDate.now(), Map.of(from, eurToUsdRate)
        );
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(from, to);

        assertThat(result).contains(BigDecimal.ONE.divide(eurToUsdRate, 6, java.math.RoundingMode.HALF_UP));
    }

    @Test
    void getExchangeRate_CrossConversion_ShouldReturnCrossRate() {
        Currency from = Currency.getInstance("EUR");
        Currency to = Currency.getInstance("GBP");
        Currency base = Currency.getInstance("USD");
        BigDecimal eurToUsd = BigDecimal.valueOf(1.18);
        BigDecimal gbpToUsd = BigDecimal.valueOf(1.25);

        when(cache.getRate(from, to)).thenReturn(Optional.empty());

        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                base, LocalDate.now(), Map.of(from, eurToUsd, to, gbpToUsd)
        );
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(from, to);

        BigDecimal expectedCrossRate = gbpToUsd.divide(eurToUsd, 6, java.math.RoundingMode.HALF_UP);
        assertThat(result).contains(expectedCrossRate);
    }

    @Test
    void getExchangeRate_WithMissingRates_ShouldReturnEmpty() {
        Currency from = Currency.getInstance("USD");
        Currency to = Currency.getInstance("JPY");
        Currency base = Currency.getInstance("USD");

        when(cache.getRate(from, to)).thenReturn(Optional.empty());

        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                base, LocalDate.now(), Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.85))
        );
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(from, to);

        assertThat(result).isEmpty();
    }

    @Test
    void getLatestRates_ShouldDelegateToProviderService() {
        CurrencyExchangeResponse expectedResponse = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(), Map.of()
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
                Currency.getInstance("USD"), LocalDate.now(), Map.of()
        );
        when(providerService.getLatestRatesFromProviders(symbols)).thenReturn(expectedResponse);

        CurrencyExchangeResponse result = exchangeRateService.getLatestRates(symbols);

        assertThat(result).isEqualTo(expectedResponse);
        verify(providerService).getLatestRatesFromProviders(symbols);
    }

    @Test
    void refreshRates_ShouldClearCacheAndFetchNewRates() {
        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.85))
        );
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        exchangeRateService.refreshRates();

        verify(cache).clearCache();
        verify(providerService).getLatestRatesFromProviders();
        verify(cache).putRate(eq(Currency.getInstance("USD")), eq(Currency.getInstance("EUR")),
                eq(BigDecimal.valueOf(0.85)), any(String.class));
    }

    @Test
    void refreshRates_WithUnsuccessfulResponse_ShouldThrowExceptionAndNotCache() {
        CurrencyExchangeResponse response = CurrencyExchangeResponse.failure();
        when(providerService.getLatestRatesFromProviders()).thenReturn(response);

        assertThatThrownBy(() -> exchangeRateService.refreshRates())
                .isInstanceOf(ExchangeRateNotAvailableException.class)
                .hasMessageContaining("Provider returned unsuccessful response");

        verify(cache).clearCache();
        verify(cache, never()).putRate(any(), any(), any(), any());
    }
}
