package com.example.cerpshashkin.service;

import com.example.cerpshashkin.dto.ConversionRequest;
import com.example.cerpshashkin.dto.ConversionResponse;
import com.example.cerpshashkin.exception.InvalidCurrencyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceUnitTest {

    @Mock
    private CurrencyConversionService conversionService;

    @Mock
    private ExchangeRateService exchangeRateService;

    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        currencyService = new CurrencyService(conversionService, exchangeRateService);
    }

    @Test
    void getSupportedCurrencies_ShouldReturnInitialCurrencies() {
        List<String> result = currencyService.getSupportedCurrencies();

        assertThat(result)
                .containsExactlyInAnyOrder("USD", "EUR", "GBP")
                .isSorted();
    }

    @Test
    void addCurrency_WithValidCurrency_ShouldAddSuccessfully() {
        String currency = "NOK";

        currencyService.addCurrency(currency);

        assertThat(currencyService.getSupportedCurrencies())
                .contains("NOK")
                .hasSize(4);
    }

    @Test
    void addCurrency_WithLowercaseCurrency_ShouldNormalizeToUppercase() {
        String currency = "nok";

        currencyService.addCurrency(currency);

        assertThat(currencyService.getSupportedCurrencies())
                .contains("NOK")
                .doesNotContain("nok");
    }

    @Test
    void addCurrency_WithWhitespaceCurrency_ShouldTrimWhitespace() {
        String currency = "  SEK  ";

        currencyService.addCurrency(currency);

        assertThat(currencyService.getSupportedCurrencies()).contains("SEK");
    }

    @Test
    void addCurrency_WithNullCurrency_ShouldThrowInvalidCurrencyException() {
        assertThatThrownBy(() -> currencyService.addCurrency(null))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("Currency code cannot be null or empty");
    }

    @Test
    void addCurrency_WithEmptyCurrency_ShouldThrowInvalidCurrencyException() {
        assertThatThrownBy(() -> currencyService.addCurrency(""))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("Currency code cannot be null or empty");
    }

    @Test
    void addCurrency_WithWhitespaceOnlyCurrency_ShouldThrowInvalidCurrencyException() {
        assertThatThrownBy(() -> currencyService.addCurrency("   "))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("Currency code cannot be null or empty");
    }

    @Test
    void addCurrency_WithInvalidCurrency_ShouldThrowInvalidCurrencyException() {
        assertThatThrownBy(() -> currencyService.addCurrency("INVALID"))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("Invalid currency code: INVALID");
    }

    @Test
    void addCurrency_WithDuplicateCurrency_ShouldNotAddDuplicate() {
        String currency = "NOK";
        currencyService.addCurrency(currency);
        int sizeAfterFirstAdd = currencyService.getSupportedCurrencies().size();

        currencyService.addCurrency(currency);

        assertThat(currencyService.getSupportedCurrencies())
                .hasSize(sizeAfterFirstAdd)
                .filteredOn(c -> c.equals("NOK"))
                .hasSize(1);
    }

    @Test
    void removeCurrency_WithExistingCurrency_ShouldRemoveSuccessfully() {
        String currency = "USD";
        assertThat(currencyService.getSupportedCurrencies()).contains("USD");

        currencyService.removeCurrency(currency);

        assertThat(currencyService.getSupportedCurrencies())
                .doesNotContain("USD")
                .hasSize(2);
    }

    @Test
    void removeCurrency_WithNullCurrency_ShouldThrowInvalidCurrencyException() {
        assertThatThrownBy(() -> currencyService.removeCurrency(null))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("Currency code cannot be null or empty");
    }

    @Test
    void removeCurrency_WithEmptyCurrency_ShouldThrowInvalidCurrencyException() {
        assertThatThrownBy(() -> currencyService.removeCurrency(""))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("Currency code cannot be null or empty");
    }

    @Test
    void convertCurrency_ShouldDelegateToConversionService() {
        ConversionRequest request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .from("USD")
                .to("EUR")
                .build();

        ConversionResponse expectedResponse = ConversionResponse.success(
                BigDecimal.valueOf(100), "USD", "EUR",
                BigDecimal.valueOf(85), BigDecimal.valueOf(0.85), "test"
        );

        when(conversionService.convertCurrency(request)).thenReturn(expectedResponse);

        ConversionResponse result = currencyService.convertCurrency(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(conversionService, times(1)).convertCurrency(request);
    }

    @Test
    void refreshExchangeRates_ShouldDelegateToExchangeRateService() {
        doNothing().when(exchangeRateService).refreshRates();

        currencyService.refreshExchangeRates();

        verify(exchangeRateService, times(1)).refreshRates();
    }

    @Test
    void multipleOperations_ShouldWorkCorrectly() {
        assertThat(currencyService.getSupportedCurrencies()).hasSize(3);

        currencyService.addCurrency("NOK");
        currencyService.addCurrency("SEK");
        assertThat(currencyService.getSupportedCurrencies()).hasSize(5);

        currencyService.removeCurrency("EUR");
        assertThat(currencyService.getSupportedCurrencies())
                .hasSize(4)
                .containsExactlyInAnyOrder("USD", "GBP", "NOK", "SEK");
    }
}
