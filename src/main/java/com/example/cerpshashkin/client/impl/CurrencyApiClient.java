package com.example.cerpshashkin.client.impl;

import com.example.cerpshashkin.client.ApiProvider;
import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.converter.CurrencyApiConverter;
import com.example.cerpshashkin.dto.CurrencyApiRawResponse;
import com.example.cerpshashkin.exception.ExternalApiException;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Component
@Slf4j
public class CurrencyApiClient implements ExchangeRateClient {

    private static final String ENDPOINT = "/latest";
    private static final String API_KEY_PARAM = "apikey";
    private static final String CURRENCIES_PARAM = "currencies";

    private static final String FETCH_LATEST_OPERATION = "fetch latest exchange rates";
    private static final String FETCH_SYMBOLS_OPERATION = "fetch exchange rates for symbols";

    private static final String FETCHING_LATEST_LOG = "Fetching latest rates from {}";
    private static final String FETCHING_SYMBOLS_LOG = "Fetching rates for symbols {} from {}";
    private static final String REQUEST_LOG = "Making request to CurrencyAPI: {}";
    private static final String SUCCESS_LOG = "Successfully received response from CurrencyAPI";
    private static final String NULL_RESPONSE_LOG = "Received null response from CurrencyAPI";
    private static final String NULL_DATA_LOG = "CurrencyAPI returned null data";
    private static final String EMPTY_DATA_LOG = "CurrencyAPI returned empty data";

    private static final String SYMBOLS_VALIDATION_ERROR = "Symbols parameter cannot be null or empty";

    @Value("${api.currencyapi.access-key}")
    private String currencyapiAccessKey;

    private final RestClient restClient;
    private final CurrencyApiConverter converter;

    @Autowired
    public CurrencyApiClient(@Qualifier("currencyapiRestClient") final RestClient restClient,
                             final CurrencyApiConverter converter) {
        this.restClient = restClient;
        this.converter = converter;
    }

    @Override
    public CurrencyExchangeResponse getLatestRates() {
        log.info(FETCHING_LATEST_LOG, getProviderName());
        return executeRequest(Optional.empty());
    }

    @Override
    public CurrencyExchangeResponse getLatestRates(final String symbols) {
        if (!StringUtils.hasText(symbols)) {
            throw new IllegalArgumentException(SYMBOLS_VALIDATION_ERROR);
        }
        log.info(FETCHING_SYMBOLS_LOG, symbols, getProviderName());
        return executeRequest(Optional.of(symbols));
    }

    @Override
    public String getProviderName() {
        return ApiProvider.CURRENCY_API.getDisplayName();
    }

    private CurrencyExchangeResponse executeRequest(final Optional<String> symbols) {
        final String url = buildUrl(symbols);
        final String operation = symbols.map(s -> FETCH_SYMBOLS_OPERATION + " " + s).orElse(FETCH_LATEST_OPERATION);

        try {
            log.debug(REQUEST_LOG, url);

            final CurrencyApiRawResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(CurrencyApiRawResponse.class);

            if (response == null) {
                log.warn(NULL_RESPONSE_LOG);
                throw new ExternalApiException(operation, getProviderName(),
                        new RuntimeException("Null response received"));
            }

            if (response.data() == null) {
                log.warn(NULL_DATA_LOG);
                throw new ExternalApiException(operation, getProviderName(),
                        new RuntimeException("Response data is null"));
            }

            if (response.data().isEmpty()) {
                log.warn(EMPTY_DATA_LOG);
                throw new ExternalApiException(operation, getProviderName(),
                        new RuntimeException("Empty data received"));
            }

            if (response.meta() == null || response.meta().lastUpdatedAt() == null) {
                log.warn("CurrencyAPI returned invalid meta information");
                throw new ExternalApiException(operation, getProviderName(),
                        new RuntimeException("Invalid meta information"));
            }

            log.debug(SUCCESS_LOG);
            return converter.convertToCurrencyExchange(response);

        } catch (Exception e) {
            throw new ExternalApiException(operation, getProviderName(), e);
        }
    }

    private String buildUrl(final Optional<String> symbols) {
        return symbols.map(s -> UriComponentsBuilder.fromUriString(ENDPOINT)
                .queryParam(API_KEY_PARAM, currencyapiAccessKey)
                .queryParam(CURRENCIES_PARAM, s)
                .build().toUriString()
        ).orElseGet(() -> UriComponentsBuilder.fromUriString(ENDPOINT)
                .queryParam(API_KEY_PARAM, currencyapiAccessKey)
                .build().toUriString()
        );
    }
}
