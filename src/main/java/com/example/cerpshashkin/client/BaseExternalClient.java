package com.example.cerpshashkin.client;

import com.example.cerpshashkin.exception.ExternalApiException;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class BaseExternalClient {

    private final RestClient restClient;

    public <T> CurrencyExchangeResponse executeApiCall(
            final String url,
            final Class<T> responseType,
            final Function<T, CurrencyExchangeResponse> converter,
            final String providerName,
            final String operation) {

        try {
            log.debug("Making request to {}: {}", providerName, url);

            final T rawResponse = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(responseType);

            if (rawResponse == null) {
                log.warn("Received null response from {} for operation: {}", providerName, operation);
                return CurrencyExchangeResponse.failure();
            }

            final CurrencyExchangeResponse result = converter.apply(rawResponse);
            if (result == null) {
                log.warn("Converter returned null result for {} response, operation: {}", providerName, operation);
                return CurrencyExchangeResponse.failure();
            }

            log.debug("Successfully processed response from {} for operation: {}", providerName, operation);
            return result;

        } catch (Exception e) {
            final String errorMessage = String.format("Failed to %s from %s: %s",
                    operation, providerName, e.getMessage());
            log.error(errorMessage, e);
            throw new ExternalApiException("Failed to " + operation + " from " + providerName, e);
        }
    }
}
