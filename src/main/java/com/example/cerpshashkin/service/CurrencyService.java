package com.example.cerpshashkin.service;

import com.example.cerpshashkin.dto.ConversionRequest;
import com.example.cerpshashkin.dto.ConversionResponse;
import com.example.cerpshashkin.exception.CurrencyNotFoundException;
import com.example.cerpshashkin.exception.InvalidCurrencyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurrencyService {

    private static final String LOG_GET_SUPPORTED = "Getting supported currencies, count: {}";
    private static final String LOG_ADD_CURRENCY = "Adding currency: {}";
    private static final String LOG_CURRENCY_ADDED_SUCCESSFULLY = "Currency {} added successfully";
    private static final String LOG_REMOVE_CURRENCY = "Removing currency: {}";
    private static final String LOG_CURRENCY_REMOVED_SUCCESSFULLY = "Currency {} removed successfully";
    private static final String LOG_CONVERT_REQUEST = "Processing currency conversion request";
    private static final String LOG_REFRESH_RATES = "Refreshing exchange rates";

    private final CurrencyConversionService conversionService;
    private final ExchangeRateService exchangeRateService;

    private final Set<String> supportedCurrencies = initializeSupportedCurrencies();

    private static Set<String> initializeSupportedCurrencies() {
        final Set<String> currencies = ConcurrentHashMap.newKeySet();
        currencies.addAll(List.of("USD", "EUR", "GBP"));
        return currencies;
    }

    public List<String> getSupportedCurrencies() {
        log.info(LOG_GET_SUPPORTED, supportedCurrencies.size());
        return supportedCurrencies.stream().sorted().toList();
    }

    public void addCurrency(final String currencyCode) {
        log.info(LOG_ADD_CURRENCY, currencyCode);

        final String normalizedCode = validateAndNormalizeCurrency(currencyCode);

        supportedCurrencies.add(normalizedCode);
        log.info(LOG_CURRENCY_ADDED_SUCCESSFULLY, normalizedCode);
    }

    public void removeCurrency(final String currencyCode) {
        log.info(LOG_REMOVE_CURRENCY, currencyCode);

        final String normalizedCode = validateAndNormalizeCurrency(currencyCode);

        if (!supportedCurrencies.contains(normalizedCode)) {
            throw new CurrencyNotFoundException(currencyCode);
        }

        supportedCurrencies.remove(normalizedCode);
        log.info(LOG_CURRENCY_REMOVED_SUCCESSFULLY, normalizedCode);
    }

    public ConversionResponse convertCurrency(final ConversionRequest request) {
        log.info(LOG_CONVERT_REQUEST);
        return conversionService.convertCurrency(request);
    }

    public void refreshExchangeRates() {
        log.info(LOG_REFRESH_RATES);
        exchangeRateService.refreshRates();
    }

    private String validateAndNormalizeCurrency(final String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new InvalidCurrencyException("Currency code cannot be null or empty");
        }

        final String normalized = currencyCode.trim().toUpperCase();

        try {
            Currency.getInstance(normalized);
            return normalized;
        } catch (IllegalArgumentException e) {
            throw new InvalidCurrencyException(currencyCode);
        }
    }
}
