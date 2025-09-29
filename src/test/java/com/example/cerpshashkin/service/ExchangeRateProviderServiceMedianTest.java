package com.example.cerpshashkin.service;

import com.example.cerpshashkin.client.ExchangeRateClient;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateProviderServiceMedianTest {

    @Mock
    private ExchangeRateClient client1;

    @Mock
    private ExchangeRateClient client2;

    @Mock
    private ExchangeRateClient client3;

    @InjectMocks
    private ExchangeRateProviderService providerService;

    @Test
    void selectMedianRates_WithOddNumberOfProviders_ShouldReturnMiddleRate() {
        CurrencyExchangeResponse response1 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.83))
        );
        CurrencyExchangeResponse response2 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.85))
        );
        CurrencyExchangeResponse response3 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.87))
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
        assertThat(result.rates().get(Currency.getInstance("EUR")))
                .isEqualByComparingTo(BigDecimal.valueOf(0.85));
    }

    @Test
    void selectMedianRates_WithEvenNumberOfProviders_ShouldReturnAverageOfMiddleTwo() {
        CurrencyExchangeResponse response1 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.83))
        );
        CurrencyExchangeResponse response2 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.85))
        );
        CurrencyExchangeResponse response3 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.86))
        );
        CurrencyExchangeResponse response4 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.88))
        );

        ExchangeRateClient client4 = org.mockito.Mockito.mock(ExchangeRateClient.class);

        when(client1.getProviderName()).thenReturn("Provider1");
        when(client1.getLatestRates()).thenReturn(response1);
        when(client2.getProviderName()).thenReturn("Provider2");
        when(client2.getLatestRates()).thenReturn(response2);
        when(client3.getProviderName()).thenReturn("Provider3");
        when(client3.getLatestRates()).thenReturn(response3);
        when(client4.getProviderName()).thenReturn("Provider4");
        when(client4.getLatestRates()).thenReturn(response4);

        providerService = new ExchangeRateProviderService(List.of(client1, client2, client3, client4));

        CurrencyExchangeResponse result = providerService.getLatestRatesFromProviders();

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.rates().get(Currency.getInstance("EUR")))
                .isEqualByComparingTo(new BigDecimal("0.855000"));
    }

    @Test
    void selectMedianRates_WithOutlier_ShouldIgnoreOutlier() {
        CurrencyExchangeResponse response1 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.85))
        );
        CurrencyExchangeResponse response2 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.86))
        );
        CurrencyExchangeResponse response3 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(999.99))
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
        assertThat(result.rates().get(Currency.getInstance("EUR")))
                .isEqualByComparingTo(BigDecimal.valueOf(0.86));
        assertThat(result.rates().get(Currency.getInstance("EUR")))
                .isNotEqualByComparingTo(BigDecimal.valueOf(999.99));
    }

    @Test
    void selectMedianRates_WithSingleProvider_ShouldReturnThatRate() {
        CurrencyExchangeResponse response = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.85))
        );

        when(client1.getProviderName()).thenReturn("Provider1");
        when(client1.getLatestRates()).thenReturn(response);

        providerService = new ExchangeRateProviderService(List.of(client1));

        CurrencyExchangeResponse result = providerService.getLatestRatesFromProviders();

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.rates().get(Currency.getInstance("EUR")))
                .isEqualByComparingTo(BigDecimal.valueOf(0.85));
    }

    @Test
    void selectMedianRates_WithMultipleCurrencies_ShouldCalculateMedianForEach() {
        CurrencyExchangeResponse response1 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(
                        Currency.getInstance("EUR"), BigDecimal.valueOf(0.85),
                        Currency.getInstance("GBP"), BigDecimal.valueOf(0.75)
                )
        );
        CurrencyExchangeResponse response2 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(
                        Currency.getInstance("EUR"), BigDecimal.valueOf(0.87),
                        Currency.getInstance("GBP"), BigDecimal.valueOf(0.77)
                )
        );

        when(client1.getProviderName()).thenReturn("Provider1");
        when(client1.getLatestRates()).thenReturn(response1);
        when(client2.getProviderName()).thenReturn("Provider2");
        when(client2.getLatestRates()).thenReturn(response2);

        providerService = new ExchangeRateProviderService(List.of(client1, client2));

        CurrencyExchangeResponse result = providerService.getLatestRatesFromProviders();

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.rates()).hasSize(2);

        assertThat(result.rates().get(Currency.getInstance("EUR")))
                .isEqualByComparingTo(new BigDecimal("0.860000"));

        assertThat(result.rates().get(Currency.getInstance("GBP")))
                .isEqualByComparingTo(new BigDecimal("0.760000"));
    }

    @Test
    void selectMedianRates_WithIdenticalRates_ShouldReturnThatRate() {
        CurrencyExchangeResponse response1 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.85))
        );
        CurrencyExchangeResponse response2 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.85))
        );
        CurrencyExchangeResponse response3 = CurrencyExchangeResponse.success(
                Currency.getInstance("USD"), LocalDate.now(),
                Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.85))
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
        assertThat(result.rates().get(Currency.getInstance("EUR")))
                .isEqualByComparingTo(BigDecimal.valueOf(0.85));
    }
}
