package com.example.cerpshashkin.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Configuration properties for external API services.
 * Validates that all required API keys and URLs are properly configured on startup.
 *
 * Maps values from application.yml:
 * external.api.fixer-url → fixerUrl
 * external.api.fixer-access-key → fixerAccessKey
 */
@Component
@ConfigurationProperties(prefix = "external.api")
@Data
@Slf4j
public class ExternalApiProperties {

    // Fixer.io
    private String fixerUrl = "http://data.fixer.io/api";
    private String fixerAccessKey = "";

    // ExchangeRatesAPI
    private String exchangeratesUrl = "http://api.exchangeratesapi.io/v1";
    private String exchangeratesAccessKey = "";

    // CurrencyAPI
    private String currencyapiUrl = "https://api.currencyapi.com/v3";
    private String currencyapiAccessKey = "";

    /**
     * Called after bean creation and field population.
     * Validates that all API keys are configured correctly.
     */
    @PostConstruct
    public void validateConfiguration() {
        log.info("Validating external API configuration...");

        validateApiConfiguration("Fixer.io", fixerUrl, fixerAccessKey);
        validateApiConfiguration("ExchangeRatesAPI", exchangeratesUrl, exchangeratesAccessKey);
        validateApiConfiguration("CurrencyAPI", currencyapiUrl, currencyapiAccessKey);

        log.info("External API configuration validation completed");
    }

    /**
     * Validates configuration for a single API.
     * Logs warnings if something is not configured.
     */
    private void validateApiConfiguration(final String apiName, final String url, final String accessKey) {
        if (!StringUtils.hasText(url)) {
            log.error("{} URL is not configured! Check application.yml", apiName);
        } else {
            log.debug("{} URL configured: {}", apiName, url);
        }

        if (!StringUtils.hasText(accessKey)) {
            log.warn("{} access key is not configured! API calls will fail. " +
                    "Set the corresponding environment variable or application property.", apiName);
        } else {
            log.debug("{} access key is configured (length: {} characters)",
                    apiName, accessKey.length());
        }
    }

    /**
     * Checks if Fixer.io is configured (has both URL and key).
     */
    public boolean isFixerConfigured() {
        return StringUtils.hasText(fixerUrl) && StringUtils.hasText(fixerAccessKey);
    }

    /**
     * Checks if ExchangeRatesAPI is configured.
     */
    public boolean isExchangeRatesConfigured() {
        return StringUtils.hasText(exchangeratesUrl) && StringUtils.hasText(exchangeratesAccessKey);
    }

    /**
     * Checks if CurrencyAPI is configured.
     */
    public boolean isCurrencyApiConfigured() {
        return StringUtils.hasText(currencyapiUrl) && StringUtils.hasText(currencyapiAccessKey);
    }

    /**
     * Returns the number of properly configured APIs (from 0 to 3).
     * Useful for fallback logic: if 0 - use mock.
     */
    public int getConfiguredApiCount() {
        int count = 0;
        if (isFixerConfigured()) count++;
        if (isExchangeRatesConfigured()) count++;
        if (isCurrencyApiConfigured()) count++;
        return count;
    }
}
