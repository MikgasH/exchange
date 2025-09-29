package com.example.cerpshashkin.service;

import com.example.cerpshashkin.client.ApiProvider;
import com.example.cerpshashkin.client.ExchangeRateClient;
import com.example.cerpshashkin.exception.AllProvidersFailedException;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRateProviderService {

    private static final String PROVIDER_SUCCESS_MESSAGE = "Successfully got rates from provider: {}";
    private static final String PROVIDER_FAILED_MESSAGE = "Provider {} failed with error: {}";
    private static final String TRYING_PROVIDER_MESSAGE = "Trying to get rates from provider: {}";
    private static final String COLLECTING_RATES_MESSAGE = "Collecting rates from all available providers";
    private static final String COLLECTED_RATES_MESSAGE = "Collected rates from {} providers";
    private static final String SELECTING_BEST_RATES_MESSAGE = "Selecting median rates for {} currency pairs";
    private static final String NORMALIZING_TO_BASE_MESSAGE = "Normalizing {} responses to common base: {}";
    private static final String USING_FALLBACK_MESSAGE = "All real providers failed, using fallback provider";
    private static final String MEDIAN_EMPTY_LIST_ERROR = "Cannot calculate median of empty list";
    private static final String LOG_WARN_CONVERSION_RATE_NOT_FOUND = "Cannot find conversion rate from {} to {}, skipping normalization";
    private static final Currency DEFAULT_BASE_CURRENCY = Currency.getInstance("USD");
    private static final int SCALE = 6;

    private final List<ExchangeRateClient> clients;

    public CurrencyExchangeResponse getLatestRatesFromProviders() {
        return getLatestRatesFromProviders(null);
    }

    public CurrencyExchangeResponse getLatestRatesFromProviders(final String symbols) {
        log.info(COLLECTING_RATES_MESSAGE);

        final List<CurrencyExchangeResponse> realProviderResponses = collectRatesFromRealProviders(symbols);

        return Optional.of(realProviderResponses)
                .filter(responses -> !responses.isEmpty())
                .map(responses -> {
                    log.info(COLLECTED_RATES_MESSAGE, responses.size());
                    return selectMedianRates(responses);
                })
                .orElseGet(() -> {
                    log.warn(USING_FALLBACK_MESSAGE);
                    return getFallbackRates(symbols);
                });
    }

    private List<CurrencyExchangeResponse> collectRatesFromRealProviders(final String symbols) {
        return clients.stream()
                .filter(client -> !ApiProvider.MOCK.getDisplayName().equals(client.getProviderName()))
                .map(client -> tryGetRatesFromClient(client, symbols))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<CurrencyExchangeResponse> tryGetRatesFromClient(
            final ExchangeRateClient client,
            final String symbols) {
        try {
            log.info(TRYING_PROVIDER_MESSAGE, client.getProviderName());

            final CurrencyExchangeResponse response = getResponseFromClient(client, symbols);

            return Optional.of(response)
                    .filter(CurrencyExchangeResponse::success)
                    .filter(r -> r.rates() != null && !r.rates().isEmpty())
                    .map(r -> {
                        log.info(PROVIDER_SUCCESS_MESSAGE, client.getProviderName());
                        return r;
                    });

        } catch (Exception e) {
            log.warn(PROVIDER_FAILED_MESSAGE, client.getProviderName(), e.getMessage());
            return Optional.empty();
        }
    }

    private CurrencyExchangeResponse getFallbackRates(final String symbols) {
        return clients.stream()
                .filter(client -> ApiProvider.MOCK.getDisplayName().equals(client.getProviderName()))
                .findFirst()
                .flatMap(client -> tryGetRatesFromClient(client, symbols))
                .orElseThrow(() -> new AllProvidersFailedException(
                        clients.stream()
                                .map(ExchangeRateClient::getProviderName)
                                .toList()
                ));
    }

    private CurrencyExchangeResponse selectMedianRates(final List<CurrencyExchangeResponse> allResponses) {
        final Currency targetBase = determineTargetBaseCurrency(allResponses);

        log.info(NORMALIZING_TO_BASE_MESSAGE, allResponses.size(), targetBase);

        final Map<Currency, List<BigDecimal>> normalizedRatesByCurrency = allResponses.stream()
                .map(response -> normalizeRatesToBase(response, targetBase))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(rates -> rates.entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(
                                Map.Entry::getValue,
                                Collectors.toList()
                        )
                ));

        final Map<Currency, BigDecimal> medianRates = normalizedRatesByCurrency.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> calculateMedian(entry.getValue())
                ));

        log.info(SELECTING_BEST_RATES_MESSAGE, medianRates.size());

        return CurrencyExchangeResponse.success(
                targetBase,
                allResponses.getFirst().rateDate(),
                medianRates
        );
    }

    private Currency determineTargetBaseCurrency(final List<CurrencyExchangeResponse> responses) {
        return responses.stream()
                .map(CurrencyExchangeResponse::base)
                .collect(Collectors.groupingBy(currency -> currency, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DEFAULT_BASE_CURRENCY);
    }

    private Optional<Map<Currency, BigDecimal>> normalizeRatesToBase(
            final CurrencyExchangeResponse response,
            final Currency targetBase) {

        final Currency sourceBase = response.base();
        final Map<Currency, BigDecimal> sourceRates = response.rates();

        if (sourceRates == null || sourceRates.isEmpty()) {
            return Optional.empty();
        }

        if (sourceBase.equals(targetBase)) {
            return Optional.of(new HashMap<>(sourceRates));
        }

        return Optional.ofNullable(sourceRates.get(targetBase))
                .map(conversionFactor -> {
                    final Map<Currency, BigDecimal> normalizedRates = sourceRates.entrySet().stream()
                            .filter(entry -> !entry.getKey().equals(targetBase))
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> entry.getValue()
                                            .divide(conversionFactor, SCALE, RoundingMode.HALF_UP)
                            ));

                    normalizedRates.put(
                            sourceBase,
                            BigDecimal.ONE.divide(conversionFactor, SCALE, RoundingMode.HALF_UP)
                    );

                    return normalizedRates;
                })
                .or(() -> {
                    log.warn(LOG_WARN_CONVERSION_RATE_NOT_FOUND,
                            sourceBase, targetBase);
                    return Optional.empty();
                });
    }

    private BigDecimal calculateMedian(final List<BigDecimal> values) {
        return Optional.of(values)
                .filter(list -> !list.isEmpty())
                .map(list -> {
                    if (list.size() == 1) {
                        return list.getFirst();
                    }

                    final List<BigDecimal> sorted = list.stream()
                            .sorted()
                            .toList();

                    final int size = sorted.size();
                    final int middle = size / 2;

                    if (size % 2 == 0) {
                        return sorted.get(middle - 1)
                                .add(sorted.get(middle))
                                .divide(BigDecimal.valueOf(2), SCALE, RoundingMode.HALF_UP);
                    }
                    return sorted.get(middle);
                })
                .orElseThrow(() -> new IllegalArgumentException(MEDIAN_EMPTY_LIST_ERROR));
    }

    private CurrencyExchangeResponse getResponseFromClient(
            final ExchangeRateClient client,
            final String symbols) {
        return Optional.ofNullable(symbols)
                .map(client::getLatestRates)
                .orElseGet(client::getLatestRates);
    }
}
