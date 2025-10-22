package com.example.cerpshashkin.service;

import com.example.cerpshashkin.dto.TrendsRequest;
import com.example.cerpshashkin.dto.TrendsResponse;
import com.example.cerpshashkin.entity.ExchangeRateEntity;
import com.example.cerpshashkin.entity.ExchangeRateSource;
import com.example.cerpshashkin.exception.InsufficientDataException;
import com.example.cerpshashkin.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrendsServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private TrendsService trendsService;

    @Test
    void calculateTrends_WithIncreasingRates_ShouldReturnPositiveChange() {
        TrendsRequest request = TrendsRequest.builder()
                .from("USD")
                .to("EUR")
                .period("24H")
                .build();

        Instant now = Instant.now();
        List<ExchangeRateEntity> rates = List.of(
                createRate(BigDecimal.valueOf(0.85), now.minus(24, ChronoUnit.HOURS)),
                createRate(BigDecimal.valueOf(0.90), now.minus(12, ChronoUnit.HOURS)),
                createRate(BigDecimal.valueOf(0.95), now)
        );

        when(exchangeRateRepository.findRatesForPeriod(
                any(Currency.class),
                any(Currency.class),
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(rates);

        TrendsResponse response = trendsService.calculateTrends(request);

        assertThat(response.success()).isTrue();
        assertThat(response.fromCurrency()).isEqualTo("USD");
        assertThat(response.toCurrency()).isEqualTo("EUR");
        assertThat(response.changePercentage()).isEqualByComparingTo(new BigDecimal("11.76"));
        assertThat(response.dataPointsCount()).isEqualTo(3);
    }

    @Test
    void calculateTrends_WithDecreasingRates_ShouldReturnNegativeChange() {
        TrendsRequest request = TrendsRequest.builder()
                .from("EUR")
                .to("USD")
                .period("12H")
                .build();

        Instant now = Instant.now();
        List<ExchangeRateEntity> rates = List.of(
                createRate(BigDecimal.valueOf(1.20), now.minus(12, ChronoUnit.HOURS)),
                createRate(BigDecimal.valueOf(1.10), now)
        );

        when(exchangeRateRepository.findRatesForPeriod(
                any(Currency.class),
                any(Currency.class),
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(rates);

        TrendsResponse response = trendsService.calculateTrends(request);

        assertThat(response.success()).isTrue();
        assertThat(response.changePercentage()).isEqualByComparingTo(new BigDecimal("-8.33"));
    }

    @Test
    void calculateTrends_WithInsufficientData_ShouldThrowException() {
        TrendsRequest request = TrendsRequest.builder()
                .from("USD")
                .to("EUR")
                .period("24H")
                .build();

        when(exchangeRateRepository.findRatesForPeriod(
                any(Currency.class),
                any(Currency.class),
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(List.of(createRate(BigDecimal.valueOf(0.85), Instant.now())));

        assertThatThrownBy(() -> trendsService.calculateTrends(request))
                .isInstanceOf(InsufficientDataException.class)
                .hasMessageContaining("Insufficient data");
    }

    @Test
    void calculateTrends_WithDifferentPeriods_ShouldCalculateCorrectStartDate() {
        TrendsRequest request = TrendsRequest.builder()
                .from("USD")
                .to("EUR")
                .period("7D")
                .build();

        Instant now = Instant.now();
        List<ExchangeRateEntity> rates = List.of(
                createRate(BigDecimal.valueOf(0.85), now.minus(7, ChronoUnit.DAYS)),
                createRate(BigDecimal.valueOf(0.90), now)
        );

        when(exchangeRateRepository.findRatesForPeriod(
                eq(Currency.getInstance("USD")),
                eq(Currency.getInstance("EUR")),
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(rates);

        trendsService.calculateTrends(request);

        verify(exchangeRateRepository).findRatesForPeriod(
                eq(Currency.getInstance("USD")),
                eq(Currency.getInstance("EUR")),
                any(Instant.class),
                any(Instant.class)
        );
    }

    private ExchangeRateEntity createRate(BigDecimal rate, Instant timestamp) {
        return ExchangeRateEntity.builder()
                .id(UUID.randomUUID())
                .baseCurrency(Currency.getInstance("USD"))
                .targetCurrency(Currency.getInstance("EUR"))
                .rate(rate)
                .source(ExchangeRateSource.AGGREGATED)
                .timestamp(timestamp)
                .build();
    }
}
