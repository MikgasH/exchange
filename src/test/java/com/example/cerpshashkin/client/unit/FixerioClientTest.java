package com.example.cerpshashkin.client.unit;

import com.example.cerpshashkin.client.impl.FixerioClient;
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
class FixerioClientTest {

    @Mock
    private RestClient restClient;

    @InjectMocks
    private FixerioClient fixerioClient;

    @Test
    void getLatestRates_WhenRestClientThrowsException_ShouldThrowExternalApiException() {
        when(restClient.get()).thenThrow(new RestClientException("Network error"));

        assertThatThrownBy(() -> fixerioClient.getLatestRates())
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Failed to fetch latest exchange rates from Fixer.io");
    }

    @Test
    void getLatestRates_WithEmptySymbols_ShouldThrowException() {
        assertThatThrownBy(() -> fixerioClient.getLatestRates(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");
    }

    @Test
    void getLatestRates_WithNullSymbols_ShouldThrowException() {
        assertThatThrownBy(() -> fixerioClient.getLatestRates(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");
    }

    @Test
    void getProviderName_ShouldReturnCorrectName() {
        assertThat(fixerioClient.getProviderName()).isEqualTo("Fixer.io");
    }
}
