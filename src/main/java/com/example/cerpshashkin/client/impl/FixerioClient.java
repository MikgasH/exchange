package com.example.cerpshashkin.client.impl;

import com.example.cerpshashkin.client.BaseExternalClient;
import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.converter.ExternalApiConverter;
import com.example.cerpshashkin.dto.FixerioResponse;
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
public class FixerioClient implements ExchangeRateClient {

    private static final String ENDPOINT = "/latest";

    @Value("${api.fixer.url}")
    private String fixerUrl;

    @Value("${api.fixer.access-key}")
    private String fixerAccessKey;

    private final BaseExternalClient baseClient;
    private final ExternalApiConverter converter;

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
        return ApiProvider.FIXER.getDisplayName();
    }

    private CurrencyExchangeResponse executeRequest(final String symbols) {
        final String url = buildUrl(symbols);
        return baseClient.executeApiCall(
                url,
                FixerioResponse.class,
                converter::convertFromFixer,
                getProviderName(),
                symbols == null ? "fetch latest exchange rates" : "fetch exchange rates for symbols " + symbols
        );
    }

    private String buildUrl(final String symbols) {
        if (symbols == null) {
            return String.format("%s%s?access_key=%s", fixerUrl, ENDPOINT, fixerAccessKey);
        }
        return String.format("%s%s?access_key=%s&symbols=%s", fixerUrl, ENDPOINT, fixerAccessKey, symbols);
    }
}
