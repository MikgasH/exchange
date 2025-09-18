package com.example.cerpshashkin.service;

import com.example.cerpshashkin.dto.ConversionRequest;
import com.example.cerpshashkin.dto.ConversionResponse;
import com.example.cerpshashkin.exception.ExchangeRateNotAvailableException;
import com.example.cerpshashkin.exception.InvalidCurrencyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurrencyConversionService {

    private static final String SAME_CURRENCY_PROVIDER = "Same Currency";
    private static final String CONVERSION_SERVICE_PROVIDER = "CurrencyConversionService";
    private static final String CURRENCY_CODE_NULL_OR_EMPTY = "Currency code cannot be null or empty";

    private static final String LOG_INFO_CONVERTING = "Converting {} {} to {}";
    private static final String LOG_ERROR_CONVERSION_FAILED = "Currency conversion failed: {}";
    private static final String LOG_INFO_CONVERSION_SUCCESS = "Conversion successful: {} {} = {} {} (rate: {})";

    private final ExchangeRateService exchangeRateService;

    public ConversionResponse convertCurrency(final ConversionRequest request) {
        log.info(LOG_INFO_CONVERTING, request.amount(), request.from(), request.to());

        try {
            final Currency fromCurrency = validateAndGetCurrency(request.from());
            final Currency toCurrency = validateAndGetCurrency(request.to());

            if (fromCurrency.equals(toCurrency)) {
                return createSameCurrencyResponse(request);
            }

            final Optional<BigDecimal> exchangeRateOpt = exchangeRateService.getExchangeRate(fromCurrency, toCurrency);

            if (exchangeRateOpt.isEmpty()) {
                throw new ExchangeRateNotAvailableException(request.from(), request.to());
            }

            return createSuccessfulConversionResponse(request, exchangeRateOpt.get());

        } catch (Exception e) {
            log.error(LOG_ERROR_CONVERSION_FAILED, e.getMessage());
            return ConversionResponse.failure(request.amount(), request.from(), request.to());
        }
    }

    private ConversionResponse createSameCurrencyResponse(final ConversionRequest request) {
        return ConversionResponse.success(
                request.amount(),
                request.from(),
                request.to(),
                request.amount(),
                BigDecimal.ONE,
                SAME_CURRENCY_PROVIDER
        );
    }

    private ConversionResponse createSuccessfulConversionResponse(final ConversionRequest request,
                                                                  final BigDecimal exchangeRate) {
        final BigDecimal convertedAmount = request.amount()
                .multiply(exchangeRate)
                .setScale(6, RoundingMode.HALF_UP);

        log.info(LOG_INFO_CONVERSION_SUCCESS,
                request.amount(), request.from(), convertedAmount, request.to(), exchangeRate);

        return ConversionResponse.success(
                request.amount(),
                request.from(),
                request.to(),
                convertedAmount,
                exchangeRate,
                CONVERSION_SERVICE_PROVIDER
        );
    }

    private Currency validateAndGetCurrency(final String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new InvalidCurrencyException(CURRENCY_CODE_NULL_OR_EMPTY);
        }

        try {
            return Currency.getInstance(currencyCode.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCurrencyException(currencyCode);
        }
    }
}
