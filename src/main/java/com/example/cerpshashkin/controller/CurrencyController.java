package com.example.cerpshashkin.controller;

import com.example.cerpshashkin.dto.ConversionRequest;
import com.example.cerpshashkin.dto.ConversionResponse;
import com.example.cerpshashkin.dto.TrendsRequest;
import com.example.cerpshashkin.dto.TrendsResponse;
import com.example.cerpshashkin.service.CurrencyService;
import com.example.cerpshashkin.validation.ValidCurrency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CurrencyController {

    private static final String LOG_GET_CURRENCIES = "GET /api/v1/currencies - getting supported currencies";
    private static final String LOG_RETURN_CURRENCIES = "Returning {} supported currencies";
    private static final String LOG_ADD_CURRENCY = "POST /api/v1/currencies - adding currency: {}";
    private static final String LOG_ADDED_SUCCESSFULLY = "Currency added: {}";
    private static final String LOG_CONVERSION_REQUEST = "GET /api/v1/currencies/exchange-rates - converting {} {} to {}";
    private static final String LOG_CONVERSION_SUCCESS = "Conversion successful: {} {} = {} {}";
    private static final String LOG_REFRESH_RATES = "POST /api/v1/currencies/refresh - refreshing exchange rates";
    private static final String LOG_RATES_REFRESHED = "Exchange rates refreshed successfully";
    private static final String LOG_TRENDS_REQUEST = "GET /api/v1/currencies/trends - getting trends for {} -> {} over {}";
    private static final String LOG_TRENDS_SUCCESS = "Trends calculated: change {}%";

    private static final String MESSAGE_CURRENCY_ADDED = "Currency %s added successfully";
    private static final String MESSAGE_RATES_UPDATED = "Exchange rates updated successfully";
    private static final String MESSAGE_CURRENCY_PARAM_REQUIRED = "Currency parameter is required";

    private final CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<List<String>> getCurrencies() {
        log.info(LOG_GET_CURRENCIES);
        final List<String> currencies = currencyService.getSupportedCurrencies();
        log.info(LOG_RETURN_CURRENCIES, currencies.size());
        return ResponseEntity.ok(currencies);
    }

    @PostMapping
    @Validated
    public ResponseEntity<String> addCurrency(
            @RequestParam
            @NotBlank(message = MESSAGE_CURRENCY_PARAM_REQUIRED)
            @ValidCurrency
            final String currency) {

        log.info(LOG_ADD_CURRENCY, currency);
        currencyService.addCurrency(currency);

        final String message = String.format(MESSAGE_CURRENCY_ADDED, currency.toUpperCase());
        log.info(LOG_ADDED_SUCCESSFULLY, currency);

        return ResponseEntity.ok(message);
    }

    @GetMapping("/exchange-rates")
    public ResponseEntity<ConversionResponse> getExchangeRates(
            @Valid @ModelAttribute final ConversionRequest request) {

        log.info(LOG_CONVERSION_REQUEST, request.amount(), request.from(), request.to());

        currencyService.validateSupportedCurrencies(request.from(), request.to());

        final ConversionResponse response = currencyService.convertCurrency(request);

        log.info(LOG_CONVERSION_SUCCESS, request.amount(), request.from(),
                response.convertedAmount(), request.to());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshRates() {
        log.info(LOG_REFRESH_RATES);
        currencyService.refreshExchangeRates();
        log.info(LOG_RATES_REFRESHED);
        return ResponseEntity.ok(MESSAGE_RATES_UPDATED);
    }

    @GetMapping("/trends")
    public ResponseEntity<TrendsResponse> getTrends(
            @Valid @ModelAttribute final TrendsRequest request) {
        log.info(LOG_TRENDS_REQUEST, request.from(), request.to(), request.period());
        currencyService.validateSupportedCurrencies(request.from(), request.to());
        final TrendsResponse response = currencyService.getTrends(request);
        log.info(LOG_TRENDS_SUCCESS, response.changePercentage());
        return ResponseEntity.ok(response);
    }
}
