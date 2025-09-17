package com.example.cerpshashkin.client.unit;

import com.example.cerpshashkin.client.impl.FixerioClient;
import com.example.cerpshashkin.converter.ExternalApiConverter;
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
class FixerioClientTest {

    @Mock
    private RestClient fixerRestClient;

    @Mock
    private ExternalApiConverter converter;

    @InjectMocks
    private FixerioClient fixerioClient;

    @Test
    void getLatestRates_WithEmptySymbols_ShouldThrowException() {
        assertThatThrownBy(() -> fixerioClient.getLatestRates(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");

        verifyNoInteractions(fixerRestClient);
        verifyNoInteractions(converter);
    }

    @Test
    void getLatestRates_WithWhitespaceSymbols_ShouldThrowException() {
        assertThatThrownBy(() -> fixerioClient.getLatestRates("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbols parameter cannot be null or empty");

        verifyNoInteractions(fixerRestClient);
        verifyNoInteractions(converter);
    }

    @Test
    void getProviderName_ShouldReturnCorrectName() {
        assertThat(fixerioClient.getProviderName()).isEqualTo("Fixer.io");
    }
}
