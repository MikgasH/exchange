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
    private static final String SUCCESS_LOG = "Successfully received response from ExchangeRatesAPI";

    private static final String SYMBOLS_VALIDATION_ERROR = "Symbols parameter cannot be null or empty";
    private static final String NULL_RESPONSE_ERROR = "Null response received";
    private static final String SUCCESS_FALSE_ERROR = "API returned success=false";
    private static final String EMPTY_RATES_ERROR = "Empty rates received";
    private static final String HTTP_ERROR_PREFIX = "HTTP error: ";

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
                    uriBuilder.path(ENDPOINT)
                            .queryParam(ACCESS_KEY_PARAM, exchangeratesAccessKey);
                    symbolsOpt.ifPresent(s -> uriBuilder.queryParam(SYMBOLS_PARAM, s));
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        (request, httpResponse) -> {
                            throw new ExternalApiException(operation, getProviderName(),
                                    HTTP_ERROR_PREFIX + httpResponse.getStatusCode());
                        })
                .body(ExchangeRatesApiResponse.class);

        validateResponse(response, operation);

        log.debug(SUCCESS_LOG);
        return converter.convertFromExchangeRates(response);
    }

    private void validateSymbols(final String symbols) {
        if (symbols != null && symbols.trim().isEmpty()) {
            throw new IllegalArgumentException(SYMBOLS_VALIDATION_ERROR);
        }
    }

    private void validateResponse(final ExchangeRatesApiResponse response, final String operation) {
        final ExchangeRatesApiResponse validResponse = Optional.ofNullable(response)
                .orElseThrow(() -> new ExternalApiException(operation, getProviderName(), NULL_RESPONSE_ERROR));

        Optional.of(validResponse)
                .filter(ExchangeRatesApiResponse::success)
                .orElseThrow(() -> new ExternalApiException(operation, getProviderName(), SUCCESS_FALSE_ERROR));

        Optional.ofNullable(validResponse.rates())
                .orElseThrow(() -> new ExternalApiException(operation, getProviderName(), EMPTY_RATES_ERROR));
    }

    @Override
    public String getProviderName() {
        return ApiProvider.EXCHANGE_RATES.getDisplayName();
    }
}
