package com.example.cerpshashkin.client.impl;

import com.example.cerpshashkin.client.BaseExternalClient;
import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.config.ExternalApiProperties;
import com.example.cerpshashkin.converter.CurrencyApiConverter;
import com.example.cerpshashkin.dto.CurrencyApiRawResponse;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrencyApiClient implements ExchangeRateClient {

    private static final String PROVIDER_NAME = "CurrencyAPI";
    private static final String ENDPOINT = "/latest";

    private final BaseExternalClient baseClient;
    private final ExternalApiProperties properties;
    private final CurrencyApiConverter converter;

    @Override
    public CurrencyExchangeResponse getLatestRates() {
        final String url = baseClient.buildCurrencyApiUrl(
                properties.getCurrencyapiUrl(),
                ENDPOINT,
                properties.getCurrencyapiAccessKey()
        );

        return baseClient.executeApiCall(
                url,
                CurrencyApiRawResponse.class,
                converter::convertToCurrencyExchange,
                PROVIDER_NAME,
                "fetch latest exchange rates"
        );
    }

    @Override
    public CurrencyExchangeResponse getLatestRates(final String symbols) {
        if (!StringUtils.hasText(symbols)) {
            throw new IllegalArgumentException("Symbols parameter cannot be null or empty");
        }

        final String url = baseClient.buildCurrencyApiUrl(
                properties.getCurrencyapiUrl(),
                ENDPOINT,
                properties.getCurrencyapiAccessKey(),
                symbols
        );

        return baseClient.executeApiCall(
                url,
                CurrencyApiRawResponse.class,
                converter::convertToCurrencyExchange,
                PROVIDER_NAME,
                "fetch exchange rates for symbols " + symbols
        );
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
}
