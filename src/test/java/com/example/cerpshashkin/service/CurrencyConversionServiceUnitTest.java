package com.example.cerpshashkin.service;

import com.example.cerpshashkin.dto.ConversionRequest;
import com.example.cerpshashkin.dto.ConversionResponse;
import com.example.cerpshashkin.exception.InvalidCurrencyException;
import com.example.cerpshashkin.exception.RateNotAvailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceUnitTest {

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private CurrencyConversionService conversionService;

    @Test
    void convertCurrency_WithSameCurrencies_ShouldReturnSameAmount() {
        final ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from("USD")
                .to("USD")
                .build();

        final ConversionResponse result = conversionService.convertCurrency(request);

        assertThat(result.success()).isTrue();
        assertThat(result.originalAmount()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(result.convertedAmount()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(result.exchangeRate()).isEqualTo(BigDecimal.ONE);
        assertThat(result.provider()).isEqualTo("Same Currency");
    }

    @Test
    void convertCurrency_WithValidDifferentCurrencies_ShouldReturnConvertedAmount() {
        final ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from("USD")
                .to("EUR")
                .build();

        when(exchangeRateService.getExchangeRate(any(Currency.class), any(Currency.class)))
                .thenReturn(Optional.of(BigDecimal.valueOf(0.85)));

        final ConversionResponse result = conversionService.convertCurrency(request);

        assertThat(result.success()).isTrue();
        assertThat(result.originalAmount()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(result.convertedAmount()).isEqualTo(new BigDecimal("85.000000"));
        assertThat(result.exchangeRate()).isEqualTo(BigDecimal.valueOf(0.85));
        assertThat(result.provider()).isEqualTo("CurrencyConversionService");

        verify(exchangeRateService).getExchangeRate(Currency.getInstance("USD"), Currency.getInstance("EUR"));
    }

    @Test
    void convertCurrency_WithNoExchangeRateAvailable_ShouldThrowException() {
        final ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from("USD")
                .to("EUR")
                .build();

        when(exchangeRateService.getExchangeRate(any(Currency.class), any(Currency.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversionService.convertCurrency(request))
                .isInstanceOf(RateNotAvailableException.class)
                .hasMessageContaining("Exchange rate not available for USD -> EUR");
    }

    @Test
    void convertCurrency_WithInvalidFromCurrency_ShouldThrowException() {
        final ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from("INVALID")
                .to("EUR")
                .build();

        assertThatThrownBy(() -> conversionService.convertCurrency(request))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("Invalid currency code: INVALID");
    }

    @Test
    void convertCurrency_WithNullFromCurrency_ShouldThrowException() {
        final ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from(null)
                .to("EUR")
                .build();

        assertThatThrownBy(() -> conversionService.convertCurrency(request))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("Currency code cannot be null or empty");
    }

    @Test
    void convertCurrency_WithEmptyFromCurrency_ShouldThrowException() {
        final ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from("")
                .to("EUR")
                .build();

        assertThatThrownBy(() -> conversionService.convertCurrency(request))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("Currency code cannot be null or empty");
    }

    @Test
    void convertCurrency_WithLowercaseCurrencies_ShouldNormalizeToUppercase() {
        final ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from("usd")
                .to("eur")
                .build();

        when(exchangeRateService.getExchangeRate(any(Currency.class), any(Currency.class)))
                .thenReturn(Optional.of(BigDecimal.valueOf(0.85)));

        final ConversionResponse result = conversionService.convertCurrency(request);

        assertThat(result.success()).isTrue();
        verify(exchangeRateService).getExchangeRate(Currency.getInstance("USD"), Currency.getInstance("EUR"));
    }

    @Test
    void convertCurrency_WithWhitespaceCurrencies_ShouldTrimWhitespace() {
        final ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from("  USD  ")
                .to("  EUR  ")
                .build();

        when(exchangeRateService.getExchangeRate(any(Currency.class), any(Currency.class)))
                .thenReturn(Optional.of(BigDecimal.valueOf(0.85)));

        final ConversionResponse result = conversionService.convertCurrency(request);

        assertThat(result.success()).isTrue();
        verify(exchangeRateService).getExchangeRate(Currency.getInstance("USD"), Currency.getInstance("EUR"));
    }

    @Test
    void convertCurrency_WithPreciseCalculation_ShouldReturnCorrectPrecision() {
        final ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(123.45))
                .from("USD")
                .to("EUR")
                .build();

        when(exchangeRateService.getExchangeRate(any(Currency.class), any(Currency.class)))
                .thenReturn(Optional.of(BigDecimal.valueOf(0.876543)));

        final ConversionResponse result = conversionService.convertCurrency(request);

        assertThat(result.success()).isTrue();
        assertThat(result.convertedAmount()).isEqualTo(new BigDecimal("108.209233"));
        assertThat(result.convertedAmount().scale()).isEqualTo(6);
    }

    @Test
    void convertCurrency_WithExchangeRateServiceException_ShouldPropagateException() {
        final ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from("USD")
                .to("EUR")
                .build();

        when(exchangeRateService.getExchangeRate(any(Currency.class), any(Currency.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        assertThatThrownBy(() -> conversionService.convertCurrency(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Service unavailable");
    }
}
