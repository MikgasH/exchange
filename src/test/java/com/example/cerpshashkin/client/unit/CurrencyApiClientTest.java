package com.example.cerpshashkin.client.unit;

import com.example.cerpshashkin.client.impl.CurrencyApiClient;
import com.example.cerpshashkin.exception.ExternalApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyApiClientTest {

    @Mock
    private RestClient restClient;

    @InjectMocks
    private CurrencyApiClient currencyApiClient;

    @Test
    void getLatestRates_WhenRestClientThrowsException_ShouldThrowExternalApiException() {
        when(restClient.get()).thenThrow(new RestClientException("Network error"));

        assertThatThrownBy(() -> currencyApiClient.getLatestRates())
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Failed to fetch latest exchange rates from CurrencyAPI");
    }

    @Test
    void getLatestRates_WithEmptySymbols_ShouldThrowException() {
        assertThatThrownBy(() -> currencyApiClient.getLatestRates(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");
    }

    @Test
    void getLatestRates_WithNullSymbols_ShouldThrowException() {
        assertThatThrownBy(() -> currencyApiClient.getLatestRates(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");
    }

    @Test
    void getProviderName_ShouldReturnCorrectName() {
        assertThat(currencyApiClient.getProviderName()).isEqualTo("CurrencyAPI");
    }
}
