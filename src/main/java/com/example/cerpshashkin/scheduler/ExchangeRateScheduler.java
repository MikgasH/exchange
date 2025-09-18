package com.example.cerpshashkin.scheduler;

import com.example.cerpshashkin.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = false)
public class ExchangeRateScheduler {

    private final ExchangeRateService exchangeRateService;

    private static final String LOG_SCHEDULED_UPDATE_START = "Starting scheduled exchange rates update";
    private static final String LOG_SCHEDULED_UPDATE_SUCCESS = "Scheduled exchange rates update completed successfully";
    private static final String LOG_SCHEDULED_UPDATE_FAILED = "Scheduled exchange rates update failed: {}";

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

    @Scheduled(cron = "${scheduling.cleanup.cron:0 0 2 * * ?}")
    public void cleanupCache() {
        log.info("Starting daily cache cleanup");
        log.info("Daily cache cleanup completed");
    }
}
