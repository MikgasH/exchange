package com.example.cerpshashkin.client;

import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

@Getter
@Component
@RequiredArgsConstructor
@Slf4j
public class BaseExternalClient {

    private final RestTemplate restTemplate;

    /**
     * Unified method for executing API calls with consistent error handling and null checks.
     *
     * @param url the complete URL to make the request to
     * @param responseType the class type for response deserialization
     * @param converter function to convert raw response to CurrencyExchangeResponse
     * @param providerName the name of the API provider for logging
     * @param operation description of the operation being performed
     * @param <T> the type of the raw API response
     * @return CurrencyExchangeResponse with success/failure status
     * @throws RuntimeException if the API call fails
     */
    public <T> CurrencyExchangeResponse executeApiCall(
            final String url,
            final Class<T> responseType,
            final Function<T, CurrencyExchangeResponse> converter,
            final String providerName,
            final String operation) {

        try {
            log.debug("Making request to {}: {}", providerName, url);

            final T rawResponse = restTemplate.getForObject(url, responseType);

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
            throw handleError(providerName, operation, e);
        }
    }

    /**
     * Builds URL for APIs that use 'access_key' parameter (Fixer.io, ExchangeRatesAPI).
     *
     * @param apiUrl the base API URL
     * @param endpoint the API endpoint
     * @param accessKey the access key for authentication
     * @return constructed URL
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public String buildUrl(final String apiUrl, final String endpoint,
                           final String accessKey) {
        validateParameters(apiUrl, endpoint, accessKey);
        return String.format("%s%s?access_key=%s", apiUrl, endpoint, accessKey);
    }

    /**
     * Builds URL for APIs that use 'access_key' parameter with symbols (Fixer.io, ExchangeRatesAPI).
     *
     * @param apiUrl the base API URL
     * @param endpoint the API endpoint
     * @param accessKey the access key for authentication
     * @param symbols comma-separated currency symbols
     * @return constructed URL
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public String buildUrl(final String apiUrl, final String endpoint,
                           final String accessKey, final String symbols) {
        validateParameters(apiUrl, endpoint, accessKey);
        if (!StringUtils.hasText(symbols)) {
            throw new IllegalArgumentException("Symbols parameter cannot be null or empty");
        }
        return String.format("%s%s?access_key=%s&symbols=%s",
                apiUrl, endpoint, accessKey, symbols);
    }

    /**
     * Builds URL for CurrencyAPI that uses 'apikey' parameter.
     *
     * @param apiUrl the base API URL
     * @param endpoint the API endpoint
     * @param apiKey the API key for authentication
     * @return constructed URL
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public String buildCurrencyApiUrl(final String apiUrl, final String endpoint,
                                      final String apiKey) {
        validateParameters(apiUrl, endpoint, apiKey);
        return String.format("%s%s?apikey=%s", apiUrl, endpoint, apiKey);
    }

    /**
     * Builds URL for CurrencyAPI that uses 'apikey' parameter with currencies.
     *
     * @param apiUrl the base API URL
     * @param endpoint the API endpoint
     * @param apiKey the API key for authentication
     * @param currencies comma-separated currency codes
     * @return constructed URL
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public String buildCurrencyApiUrl(final String apiUrl, final String endpoint,
                                      final String apiKey, final String currencies) {
        validateParameters(apiUrl, endpoint, apiKey);
        if (!StringUtils.hasText(currencies)) {
            throw new IllegalArgumentException("Currencies parameter cannot be null or empty");
        }
        return String.format("%s%s?apikey=%s&currencies=%s",
                apiUrl, endpoint, apiKey, currencies);
    }

    /**
     * Handles errors from external API calls in a consistent way.
     *
     * @param providerName the name of the API provider
     * @param operation the operation that failed
     * @param e the exception that occurred
     * @return RuntimeException with standardized error message
     */
    public RuntimeException handleError(final String providerName,
                                        final String operation,
                                        final Exception e) {
        final String errorMessage = String.format("Failed to %s from %s: %s",
                operation, providerName, e.getMessage());
        log.error(errorMessage, e);
        return new RuntimeException("Failed to " + operation + " from " + providerName, e);
    }

    /**
     * Validates that required parameters are not null or empty.
     *
     * @param params the parameters to validate
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    private void validateParameters(final String... params) {
        for (int i = 0; i < params.length; i++) {
            if (!StringUtils.hasText(params[i])) {
                throw new IllegalArgumentException(
                        String.format("Parameter at index %d cannot be null or empty", i));
            }
        }
    }
}
