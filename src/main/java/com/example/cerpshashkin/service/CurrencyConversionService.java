package com.example.cerpshashkin.service;

import com.example.cerpshashkin.dto.ConversionRequest;
import com.example.cerpshashkin.dto.ConversionResponse;
import com.example.cerpshashkin.exception.InvalidCurrencyException;
import com.example.cerpshashkin.exception.RateNotAvailableException;
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
    private static final String LOG_CONVERTING = "Converting {} {} to {}";
    private static final String LOG_CONVERSION_SUCCESS = "Conversion successful: {} {} = {} {} (rate: {})";
    private static final int SCALE = 6;
    private static final String ERROR_CURRENCY_NULL_OR_EMPTY = "Currency code cannot be null or empty";

    private final ExchangeRateService exchangeRateService;

    public ConversionResponse convertCurrency(final ConversionRequest request) {
        log.info(LOG_CONVERTING, request.amount(), request.from(), request.to());

        final Currency fromCurrency = parseCurrency(request.from());
        final Currency toCurrency = parseCurrency(request.to());

        return Optional.of(fromCurrency)
                .filter(from -> from.equals(toCurrency))
                .map(from -> createSameCurrencyResponse(request))
                .orElseGet(() -> exchangeRateService.getExchangeRate(fromCurrency, toCurrency)
                        .map(rate -> createSuccessfulConversionResponse(request, rate))
                        .orElseThrow(() -> new RateNotAvailableException(request.from(), request.to()))
                );
    }

    private Currency parseCurrency(final String currencyCode) {
        if (currencyCode == null) {
            throw new InvalidCurrencyException(ERROR_CURRENCY_NULL_OR_EMPTY);
        }

        final String trimmed = currencyCode.trim();

        if (trimmed.isEmpty()) {
            throw new InvalidCurrencyException(ERROR_CURRENCY_NULL_OR_EMPTY);
        }

        try {
            return Currency.getInstance(trimmed.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCurrencyException(currencyCode);
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

    private ConversionResponse createSuccessfulConversionResponse(
            final ConversionRequest request,
            final BigDecimal exchangeRate) {

        final BigDecimal convertedAmount = request.amount()
                .multiply(exchangeRate)
                .setScale(SCALE, RoundingMode.HALF_UP);

        log.info(LOG_CONVERSION_SUCCESS,
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
}
