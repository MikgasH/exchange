package com.example.cerpshashkin.scheduler;

import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import com.example.cerpshashkin.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateSchedulerTest {

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private List<ExchangeRateClient> clients;

    @Mock
    private ExchangeRateClient mockClient;

    @InjectMocks
    private ExchangeRateScheduler scheduler;

    @Test
    void initializeExchangeRates_WithSuccessfulRefresh_ShouldComplete() {
        doNothing().when(exchangeRateService).refreshRates();

        scheduler.initializeExchangeRates();

        verify(exchangeRateService, times(1)).refreshRates();
    }

    @Test
    void initializeExchangeRates_WithFailure_ShouldAttemptFallback() {
        doThrow(new RuntimeException("All providers failed"))
                .when(exchangeRateService).refreshRates();

        when(clients.stream()).thenReturn(List.of(mockClient).stream());
        when(mockClient.getProviderName()).thenReturn("MockAPI");

        CurrencyExchangeResponse mockResponse = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"),
                LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.85))
        );
        when(mockClient.getLatestRates()).thenReturn(mockResponse);
        doNothing().when(exchangeRateService).cacheExchangeRates(mockResponse);

        scheduler.initializeExchangeRates();

        verify(exchangeRateService, times(1)).refreshRates();
        verify(mockClient, times(1)).getLatestRates();
        verify(exchangeRateService, times(1)).cacheExchangeRates(mockResponse);
    }

    @Test
    void initializeExchangeRates_WithFallbackFailure_ShouldNotThrow() {
        doThrow(new RuntimeException("All providers failed"))
                .when(exchangeRateService).refreshRates();

        when(clients.stream()).thenReturn(List.of(mockClient).stream());
        when(mockClient.getProviderName()).thenReturn("MockAPI");
        when(mockClient.getLatestRates()).thenThrow(new RuntimeException("Mock also failed"));

        scheduler.initializeExchangeRates();

        verify(exchangeRateService, times(1)).refreshRates();
        verify(mockClient, times(1)).getLatestRates();
    }

    @Test
    void updateExchangeRates_WithSuccessfulRefresh_ShouldComplete() {
        doNothing().when(exchangeRateService).refreshRates();

        scheduler.updateExchangeRates();

        verify(exchangeRateService, times(1)).refreshRates();
    }

    @Test
    void updateExchangeRates_WithFailure_ShouldLogError() {
        doThrow(new RuntimeException("Update failed"))
                .when(exchangeRateService).refreshRates();

        scheduler.updateExchangeRates();

        verify(exchangeRateService, times(1)).refreshRates();
    }
}
