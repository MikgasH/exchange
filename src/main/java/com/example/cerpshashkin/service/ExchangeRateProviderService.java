package com.example.cerpshashkin.service;

import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.exception.AllProvidersFailedException;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRateProviderService {

    private static final String PROVIDER_SUCCESS_MESSAGE = "Successfully got rates from provider: {}";
    private static final String PROVIDER_FAILED_MESSAGE = "Provider {} failed with error: {}";
    private static final String PROVIDER_UNSUCCESSFUL_MESSAGE = "Provider {} returned unsuccessful response";
    private static final String TRYING_PROVIDER_MESSAGE = "Trying to get rates from provider: {}";

    private final List<ExchangeRateClient> clients;

    public CurrencyExchangeResponse getLatestRatesFromProviders() {
        return getLatestRatesFromProviders(null);
    }

    public CurrencyExchangeResponse getLatestRatesFromProviders(final String symbols) {
        final List<String> failedProviders = new ArrayList<>();

        for (ExchangeRateClient client : clients) {
            try {
                log.info(TRYING_PROVIDER_MESSAGE, client.getProviderName());

                final CurrencyExchangeResponse response = getResponseFromClient(client, symbols);

                if (response.success()) {
                    log.info(PROVIDER_SUCCESS_MESSAGE, client.getProviderName());
                    return response;
                } else {
                    failedProviders.add(client.getProviderName());
                    log.warn(PROVIDER_UNSUCCESSFUL_MESSAGE, client.getProviderName());
                }

            } catch (Exception e) {
                failedProviders.add(client.getProviderName());
                log.error(PROVIDER_FAILED_MESSAGE, client.getProviderName(), e.getMessage(), e);
            }
        }

        throw new AllProvidersFailedException(failedProviders);
    }

    private CurrencyExchangeResponse getResponseFromClient(final ExchangeRateClient client, final String symbols) {
        if (symbols == null) {
            return client.getLatestRates();
        } else {
            return client.getLatestRates(symbols);
        }
    }
}
