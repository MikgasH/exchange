package com.example.cerpshashkin.client.impl;

import com.example.cerpshashkin.client.ApiProvider;
import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.converter.ExternalApiConverter;
import com.example.cerpshashkin.dto.FixerioResponse;
import com.example.cerpshashkin.exception.ExternalApiException;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class MockService1Client implements ExchangeRateClient {

    private static final String ENDPOINT = "/api/rates/latest";
    private static final String SYMBOLS_PARAM = "symbols";
    private static final String OPERATION_LATEST = "fetch latest exchange rates";
    private static final String OPERATION_SYMBOLS = "fetch exchange rates for symbols";
    private static final String HTTP_ERROR_PREFIX = "HTTP error: ";

    private final RestClient mockService1RestClient;
    private final ExternalApiConverter converter;

    @Override
    public CurrencyExchangeResponse getLatestRates() {
        return getLatestRates(null);
    }

    @Override
    public CurrencyExchangeResponse getLatestRates(final String symbols) {
        final Optional<String> symbolsOpt = Optional.ofNullable(symbols)
                .filter(StringUtils::hasText);

        final String operation = symbolsOpt
                .map(s -> OPERATION_SYMBOLS + " " + s)
                .orElse(OPERATION_LATEST);

        log.info("Fetching rates from {}", getProviderName());

        final FixerioResponse response = mockService1RestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(ENDPOINT);
                    symbolsOpt.ifPresent(s -> uriBuilder.queryParam(SYMBOLS_PARAM, s));
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        (request, httpResponse) -> {
                            throw new ExternalApiException(operation, getProviderName(),
                                    HTTP_ERROR_PREFIX + httpResponse.getStatusCode());
                        })
                .body(FixerioResponse.class);

        return converter.convertFromFixer(response);
    }

    @Override
    public String getProviderName() {
        return ApiProvider.MOCK_SERVICE_1.getDisplayName();
    }
}
