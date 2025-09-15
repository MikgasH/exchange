package com.example.cerpshashkin.client.impl;

import com.example.cerpshashkin.client.BaseExternalClient;
import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.converter.CurrencyApiConverter;
import com.example.cerpshashkin.dto.CurrencyApiRawResponse;
import com.example.cerpshashkin.client.ApiProvider;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrencyApiClient implements ExchangeRateClient {

    private static final String ENDPOINT = "/latest";

    @Value("${api.currencyapi.url}")
    private String currencyapiUrl;

    @Value("${api.currencyapi.access-key}")
    private String currencyapiAccessKey;

    private final BaseExternalClient baseClient;
    private final CurrencyApiConverter converter;

    @Override
    public CurrencyExchangeResponse getLatestRates() {
        log.info("Fetching latest rates from {}", getProviderName());
        return executeRequest(null);
    }

    @Override
    public CurrencyExchangeResponse getLatestRates(final String symbols) {
        if (!StringUtils.hasText(symbols)) {
            throw new IllegalArgumentException("Symbols parameter cannot be null or empty");
        }
        log.info("Fetching rates for symbols {} from {}", symbols, getProviderName());
        return executeRequest(symbols);
    }

    @Override
    public String getProviderName() {
        return ApiProvider.CURRENCY_API.getDisplayName();
    }

    private CurrencyExchangeResponse executeRequest(final String symbols) {
        final String url = buildUrl(symbols);
        return baseClient.executeApiCall(
                url,
                CurrencyApiRawResponse.class,
                converter::convertToCurrencyExchange,
                getProviderName(),
                symbols == null ? "fetch latest exchange rates" : "fetch exchange rates for symbols " + symbols
        );
    }

    private String buildUrl(final String symbols) {
        if (symbols == null) {
            return String.format("%s%s?apikey=%s", currencyapiUrl, ENDPOINT, currencyapiAccessKey);
        }
        return String.format("%s%s?apikey=%s&currencies=%s", currencyapiUrl, ENDPOINT, currencyapiAccessKey, symbols);
    }
}
