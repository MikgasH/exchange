package com.example.cerpshashkin.client.impl;

import com.example.cerpshashkin.client.ApiProvider;
import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.converter.ExternalApiConverter;
import com.example.cerpshashkin.dto.ExchangeRatesApiResponse;
import com.example.cerpshashkin.exception.ExternalApiException;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExchangeRatesClient implements ExchangeRateClient {

    private static final String ENDPOINT = "/latest";
    private static final String ACCESS_KEY_PARAM = "access_key";
    private static final String SYMBOLS_PARAM = "symbols";

    private static final String OPERATION_LATEST = "fetch latest exchange rates";
    private static final String OPERATION_SYMBOLS = "fetch exchange rates for symbols";

    private static final String FETCHING_LATEST_LOG = "Fetching latest rates from {}";
    private static final String FETCHING_SYMBOLS_LOG = "Fetching rates for symbols {} from {}";
    private static final String REQUEST_LOG = "Making request to ExchangeRatesAPI: {}";

    private static final String SYMBOLS_VALIDATION_ERROR = "Symbols parameter cannot be null or empty";

    @Value("${api.exchangerates.access-key}")
    private String exchangeratesAccessKey;

    private final RestClient exchangeratesRestClient;
    private final ExternalApiConverter converter;

    @Override
    public CurrencyExchangeResponse getLatestRates() {
        return getLatestRates(null);
    }

    @Override
    public CurrencyExchangeResponse getLatestRates(final String symbols) {
        validateSymbols(symbols);

        final Optional<String> symbolsOpt = Optional.ofNullable(symbols)
                .filter(StringUtils::hasText);

        final String operation = symbolsOpt
                .map(s -> OPERATION_SYMBOLS + " " + s)
                .orElse(OPERATION_LATEST);

        if (symbolsOpt.isPresent()) {
            log.info(FETCHING_SYMBOLS_LOG, symbols, getProviderName());
        } else {
            log.info(FETCHING_LATEST_LOG, getProviderName());
        }

        log.debug(REQUEST_LOG, ENDPOINT + symbolsOpt.map(s -> " with symbols: " + s).orElse(""));

        final ExchangeRatesApiResponse response = exchangeratesRestClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path(ENDPOINT)
                            .queryParam(ACCESS_KEY_PARAM, exchangeratesAccessKey);
                    symbolsOpt.ifPresent(s -> builder.queryParam(SYMBOLS_PARAM, s));
                    return builder.build();
                })
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        (request, httpResponse) -> {
                            throw new ExternalApiException(operation, getProviderName(),
                                    "HTTP error: " + httpResponse.getStatusCode());
                        })
                .body(ExchangeRatesApiResponse.class);

        validateResponse(response, operation);

        log.debug("Successfully received response from ExchangeRatesAPI");
        return converter.convertFromExchangeRates(response);
    }

    private void validateSymbols(final String symbols) {
        if (symbols != null && symbols.trim().isEmpty()) {
            throw new IllegalArgumentException(SYMBOLS_VALIDATION_ERROR);
        }
    }

    private void validateResponse(final ExchangeRatesApiResponse response, final String operation) {
        if (response == null) {
            throw new ExternalApiException(operation, getProviderName(), "Null response received");
        }

        if (!response.success()) {
            throw new ExternalApiException(operation, getProviderName(), "API returned success=false");
        }

        if (response.rates() == null) {
            throw new ExternalApiException(operation, getProviderName(), "Empty rates received");
        }
    }

    @Override
    public String getProviderName() {
        return ApiProvider.EXCHANGE_RATES.getDisplayName();
    }
}
