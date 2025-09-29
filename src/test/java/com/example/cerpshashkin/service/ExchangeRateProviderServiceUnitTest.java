package com.example.cerpshashkin.service;

import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.exception.AllProvidersFailedException;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateProviderServiceUnitTest {

    @Mock
    private ExchangeRateClient client1;

    @Mock
    private ExchangeRateClient client2;

    @Mock
    private ExchangeRateClient client3;

    @InjectMocks
    private ExchangeRateProviderService providerService;

    @Test
    void getLatestRatesFromProviders_WithAllProvidersSuccess_ShouldReturnMedianRates() {
        CurrencyExchangeResponse response1 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.85))
        );
        CurrencyExchangeResponse response2 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.87))
        );
        CurrencyExchangeResponse response3 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.86))
        );

        when(client1.getProviderName()).thenReturn("Provider1");
        when(client1.getLatestRates()).thenReturn(response1);
        when(client2.getProviderName()).thenReturn("Provider2");
        when(client2.getLatestRates()).thenReturn(response2);
        when(client3.getProviderName()).thenReturn("Provider3");
        when(client3.getLatestRates()).thenReturn(response3);

        providerService = new ExchangeRateProviderService(List.of(client1, client2, client3));

        CurrencyExchangeResponse result = providerService.getLatestRatesFromProviders();

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.base()).isEqualTo(Currency.getInstance("USD"));
        assertThat(result.rates()).containsKeys(
                Currency.getInstance("EUR")
        );
        assertThat(result.rates().get(Currency.getInstance("EUR")))
                .isEqualByComparingTo(BigDecimal.valueOf(0.86));
    }

    @Test
    void getLatestRatesFromProviders_WithOneProviderFail_ShouldUseOthers() {
        CurrencyExchangeResponse response2 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.87))
        );
        CurrencyExchangeResponse response3 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.86))
        );

        when(client1.getProviderName()).thenReturn("Provider1");
        when(client1.getLatestRates()).thenThrow(new RuntimeException("Provider1 failed"));
        when(client2.getProviderName()).thenReturn("Provider2");
        when(client2.getLatestRates()).thenReturn(response2);
        when(client3.getProviderName()).thenReturn("Provider3");
        when(client3.getLatestRates()).thenReturn(response3);

        providerService = new ExchangeRateProviderService(List.of(client1, client2, client3));

        CurrencyExchangeResponse result = providerService.getLatestRatesFromProviders();

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.rates().get(Currency.getInstance("EUR")))
                .isEqualByComparingTo(new BigDecimal("0.865000"));

        verify(client1).getLatestRates();
        verify(client2).getLatestRates();
        verify(client3).getLatestRates();
    }

    @Test
    void getLatestRatesFromProviders_WithUnsuccessfulResponse_ShouldIgnoreIt() {
        CurrencyExchangeResponse failureResponse = CurrencyExchangeResponse.failure();
        CurrencyExchangeResponse successResponse = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.87))
        );

        when(client1.getProviderName()).thenReturn("Provider1");
        when(client1.getLatestRates()).thenReturn(failureResponse);
        when(client2.getProviderName()).thenReturn("Provider2");
        when(client2.getLatestRates()).thenReturn(successResponse);
        when(client3.getProviderName()).thenReturn("Provider3");
        when(client3.getLatestRates()).thenReturn(successResponse);

        providerService = new ExchangeRateProviderService(List.of(client1, client2, client3));

        CurrencyExchangeResponse result = providerService.getLatestRatesFromProviders();

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();

        verify(client1).getLatestRates();
        verify(client2).getLatestRates();
        verify(client3).getLatestRates();
    }

    @Test
    void getLatestRatesFromProviders_WithAllProvidersFail_ShouldThrowException() {
        when(client1.getProviderName()).thenReturn("Provider1");
        when(client1.getLatestRates()).thenThrow(new RuntimeException("Provider1 failed"));
        when(client2.getProviderName()).thenReturn("Provider2");
        when(client2.getLatestRates()).thenThrow(new RuntimeException("Provider2 failed"));
        when(client3.getProviderName()).thenReturn("Provider3");
        when(client3.getLatestRates()).thenThrow(new RuntimeException("Provider3 failed"));

        providerService = new ExchangeRateProviderService(List.of(client1, client2, client3));

        assertThatThrownBy(() -> providerService.getLatestRatesFromProviders())
                .isInstanceOf(AllProvidersFailedException.class)
                .hasMessageContaining("All exchange rate providers failed")
                .hasMessageContaining("Provider1")
                .hasMessageContaining("Provider2")
                .hasMessageContaining("Provider3");

        verify(client1).getLatestRates();
        verify(client2).getLatestRates();
        verify(client3).getLatestRates();
    }

    @Test
    void getLatestRatesFromProviders_WithSymbols_ShouldCallCorrectMethod() {
        String symbols = "EUR,GBP";
        CurrencyExchangeResponse successResponse = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.87))
        );

        when(client1.getProviderName()).thenReturn("Provider1");
        when(client1.getLatestRates(symbols)).thenReturn(successResponse);
        when(client2.getProviderName()).thenReturn("Provider2");
        when(client2.getLatestRates(symbols)).thenReturn(successResponse);
        when(client3.getProviderName()).thenReturn("Provider3");
        when(client3.getLatestRates(symbols)).thenReturn(successResponse);

        providerService = new ExchangeRateProviderService(List.of(client1, client2, client3));

        CurrencyExchangeResponse result = providerService.getLatestRatesFromProviders(symbols);

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();

        verify(client1).getLatestRates(symbols);
        verify(client1, never()).getLatestRates();
        verify(client2).getLatestRates(symbols);
        verify(client3).getLatestRates(symbols);
    }

    @Test
    void getLatestRatesFromProviders_WithNullSymbols_ShouldCallDefaultMethod() {
        CurrencyExchangeResponse successResponse = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.87))
        );

        when(client1.getProviderName()).thenReturn("Provider1");
        when(client1.getLatestRates()).thenReturn(successResponse);
        when(client2.getProviderName()).thenReturn("Provider2");
        when(client2.getLatestRates()).thenReturn(successResponse);
        when(client3.getProviderName()).thenReturn("Provider3");
        when(client3.getLatestRates()).thenReturn(successResponse);

        providerService = new ExchangeRateProviderService(List.of(client1, client2, client3));

        CurrencyExchangeResponse result = providerService.getLatestRatesFromProviders(null);

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();

        verify(client1).getLatestRates();
        verify(client1, never()).getLatestRates(any(String.class));
        verify(client2).getLatestRates();
        verify(client3).getLatestRates();
    }

    @Test
    void getLatestRatesFromProviders_WithMixedFailures_ShouldReturnMedianFromSuccessful() {
        String symbols = "EUR,GBP";
        CurrencyExchangeResponse failureResponse = CurrencyExchangeResponse.failure();
        CurrencyExchangeResponse successResponse = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.87))
        );

        when(client1.getProviderName()).thenReturn("Provider1");
        when(client1.getLatestRates(symbols)).thenThrow(new RuntimeException("Exception"));
        when(client2.getProviderName()).thenReturn("Provider2");
        when(client2.getLatestRates(symbols)).thenReturn(failureResponse);
        when(client3.getProviderName()).thenReturn("Provider3");
        when(client3.getLatestRates(symbols)).thenReturn(successResponse);

        providerService = new ExchangeRateProviderService(List.of(client1, client2, client3));

        CurrencyExchangeResponse result = providerService.getLatestRatesFromProviders(symbols);

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.rates().get(Currency.getInstance("EUR")))
                .isEqualByComparingTo(BigDecimal.valueOf(0.87));

        verify(client1).getLatestRates(symbols);
        verify(client2).getLatestRates(symbols);
        verify(client3).getLatestRates(symbols);
    }

    @Test
    void getLatestRatesFromProviders_WithMultipleCurrencies_ShouldSelectMedianForEach() {
        CurrencyExchangeResponse response1 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(
                        Currency.getInstance("EUR"), BigDecimal.valueOf(0.85),
                        Currency.getInstance("GBP"), BigDecimal.valueOf(0.76)
                )
        );
        CurrencyExchangeResponse response2 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(
                        Currency.getInstance("EUR"), BigDecimal.valueOf(0.87),
                        Currency.getInstance("GBP"), BigDecimal.valueOf(0.75)
                )
        );
        CurrencyExchangeResponse response3 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(
                        Currency.getInstance("EUR"), BigDecimal.valueOf(0.83),
                        Currency.getInstance("GBP"), BigDecimal.valueOf(0.77)
                )
        );

        when(client1.getProviderName()).thenReturn("Provider1");
        when(client1.getLatestRates()).thenReturn(response1);
        when(client2.getProviderName()).thenReturn("Provider2");
        when(client2.getLatestRates()).thenReturn(response2);
        when(client3.getProviderName()).thenReturn("Provider3");
        when(client3.getLatestRates()).thenReturn(response3);

        providerService = new ExchangeRateProviderService(List.of(client1, client2, client3));

        CurrencyExchangeResponse result = providerService.getLatestRatesFromProviders();

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.rates()).hasSize(2);
        assertThat(result.rates().get(Currency.getInstance("EUR")))
                .isEqualByComparingTo(BigDecimal.valueOf(0.85));
        assertThat(result.rates().get(Currency.getInstance("GBP")))
                .isEqualByComparingTo(BigDecimal.valueOf(0.76));
    }
}
