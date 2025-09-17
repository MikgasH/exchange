package com.example.cerpshashkin.client.unit;

import com.example.cerpshashkin.client.impl.CurrencyApiClient;
import com.example.cerpshashkin.converter.CurrencyApiConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CurrencyApiClientTest {

    @Mock
    private RestClient currencyapiRestClient;

    @Mock
    private CurrencyApiConverter converter;

    @InjectMocks
    private CurrencyApiClient currencyApiClient;

    @Test
    void getLatestRates_WithEmptySymbols_ShouldThrowException() {
        assertThatThrownBy(() -> currencyApiClient.getLatestRates(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");

        verifyNoInteractions(currencyapiRestClient);
        verifyNoInteractions(converter);
    }

    @Test
    void getLatestRates_WithWhitespaceSymbols_ShouldThrowException() {
        assertThatThrownBy(() -> currencyApiClient.getLatestRates("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");

        verifyNoInteractions(currencyapiRestClient);
        verifyNoInteractions(converter);
    }

    @Test
    void getProviderName_ShouldReturnCorrectName() {
        assertThat(currencyApiClient.getProviderName()).isEqualTo("CurrencyAPI");
    }
}
