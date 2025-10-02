package com.example.cerpshashkin.scheduler;

import com.example.cerpshashkin.client.ApiProvider;
import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import com.example.cerpshashkin.service.ExchangeRateService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true")
public class ExchangeRateScheduler {

    private static final String LOG_INIT_START = "Initializing exchange rates on application startup";
    private static final String LOG_INIT_SUCCESS = "Exchange rates initialized successfully";
    private static final String LOG_INIT_FAILED = "Failed to initialize exchange rates: {}";
    private static final String LOG_INIT_FALLBACK = "Initializing with fallback Mock data";
    private static final String LOG_INIT_FALLBACK_SUCCESS = "Successfully initialized with fallback Mock data";
    private static final String LOG_INIT_FALLBACK_FAILED = "Even fallback initialization failed: {}";

    private static final String LOG_SCHEDULED_UPDATE_START = "Starting scheduled exchange rates update";
    private static final String LOG_SCHEDULED_UPDATE_SUCCESS = "Scheduled exchange rates update completed successfully";
    private static final String LOG_SCHEDULED_UPDATE_FAILED = "Scheduled exchange rates update failed: {}";

    private static final String MOCK_PROVIDER_NOT_FOUND_ERROR = "Mock provider not found";
    private static final String MOCK_UNSUCCESSFUL_RESPONSE_ERROR = "Mock provider returned unsuccessful response";

    private final ExchangeRateService exchangeRateService;
    private final List<ExchangeRateClient> clients;

    @PostConstruct
    public void initializeExchangeRates() {
        log.info(LOG_INIT_START);
        try {
            exchangeRateService.refreshRates();
            log.info(LOG_INIT_SUCCESS);
        } catch (Exception e) {
            log.error(LOG_INIT_FAILED, e.getMessage(), e);
            try {
                initializeFallbackRates();
            } catch (Exception fallbackException) {
                log.error(LOG_INIT_FALLBACK_FAILED, fallbackException.getMessage(), fallbackException);
            }
        }
    }

    @Scheduled(fixedRateString = "${scheduling.exchange-rates.rate:3600000}")
    public void updateExchangeRates() {
        log.info(LOG_SCHEDULED_UPDATE_START);
        try {
            exchangeRateService.refreshRates();
            log.info(LOG_SCHEDULED_UPDATE_SUCCESS);
        } catch (Exception e) {
            log.error(LOG_SCHEDULED_UPDATE_FAILED, e.getMessage(), e);
        }
    }

    private void initializeFallbackRates() {
        log.warn(LOG_INIT_FALLBACK);

        final ExchangeRateClient mockClient = clients.stream()
                .filter(client -> ApiProvider.MOCK.getDisplayName().equals(client.getProviderName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(MOCK_PROVIDER_NOT_FOUND_ERROR));

        final CurrencyExchangeResponse mockResponse = mockClient.getLatestRates();

        if (!mockResponse.success() || mockResponse.rates() == null || mockResponse.rates().isEmpty()) {
            throw new IllegalStateException(MOCK_UNSUCCESSFUL_RESPONSE_ERROR);
        }

        cacheExchangeRates(mockResponse);

        log.info(LOG_INIT_FALLBACK_SUCCESS);
    }

    private void cacheExchangeRates(final CurrencyExchangeResponse response) {
        exchangeRateService.cacheExchangeRates(response);
    }
}
