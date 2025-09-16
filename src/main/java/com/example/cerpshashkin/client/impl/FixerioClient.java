package com.example.cerpshashkin.client.impl;

import com.example.cerpshashkin.client.ApiProvider;
import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.converter.ExternalApiConverter;
import com.example.cerpshashkin.dto.FixerioResponse;
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
public class FixerioClient implements ExchangeRateClient {

    private static final String ENDPOINT = "/latest";
    private static final String ACCESS_KEY_PARAM = "access_key";
    private static final String SYMBOLS_PARAM = "symbols";

    private static final String FETCH_LATEST_OPERATION = "fetch latest exchange rates";
    private static final String FETCH_SYMBOLS_OPERATION = "fetch exchange rates for symbols";

    private static final String FETCHING_LATEST_LOG = "Fetching latest rates from {}";
    private static final String FETCHING_SYMBOLS_LOG = "Fetching rates for symbols {} from {}";
    private static final String REQUEST_LOG = "Making request to Fixer.io: {}";
    private static final String SUCCESS_LOG = "Successfully received response from Fixer.io";
    private static final String NULL_RESPONSE_LOG = "Received null response from Fixer.io";

    private static final String SYMBOLS_VALIDATION_ERROR = "Symbols parameter cannot be null or empty";

    @Value("${api.fixer.access-key}")
    private String fixerAccessKey;

    private final RestClient restClient;
    private final ExternalApiConverter converter;

    @Autowired
    public FixerioClient(@Qualifier("fixerRestClient") final RestClient restClient,
                         final ExternalApiConverter converter) {
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
        return ApiProvider.FIXER.getDisplayName();
    }

    private CurrencyExchangeResponse executeRequest(final Optional<String> symbols) {
        final String url = buildUrl(symbols);
        final String operation = symbols.map(s -> FETCH_SYMBOLS_OPERATION + " " + s).orElse(FETCH_LATEST_OPERATION);

        try {
            log.debug(REQUEST_LOG, url);

            final FixerioResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(FixerioResponse.class);

            if (response == null) {
                log.warn(NULL_RESPONSE_LOG);
                throw new ExternalApiException(operation, getProviderName(),
                        new RuntimeException("Null response received"));
            }

            if (!response.success()) {
                log.warn("Fixer.io returned unsuccessful response");
                throw new ExternalApiException(operation, getProviderName(),
                        new RuntimeException("API returned success=false"));
            }

            if (response.rates() == null || response.rates().isEmpty()) {
                log.warn("Fixer.io returned empty rates");
                throw new ExternalApiException(operation, getProviderName(),
                        new RuntimeException("Empty rates received"));
            }

            log.debug(SUCCESS_LOG);
            return converter.convertFromFixer(response);

        } catch (Exception e) {
            throw new ExternalApiException(operation, getProviderName(), e);
        }
    }

    private String buildUrl(final Optional<String> symbols) {
        return symbols.map(s -> UriComponentsBuilder.fromUriString(ENDPOINT)
                .queryParam(ACCESS_KEY_PARAM, fixerAccessKey)
                .queryParam(SYMBOLS_PARAM, s)
                .build().toUriString()
        ).orElseGet(() -> UriComponentsBuilder.fromUriString(ENDPOINT)
                .queryParam(ACCESS_KEY_PARAM, fixerAccessKey)
                .build().toUriString()
        );
    }
}
