package com.example.cerpshashkin.controller;

import com.example.cerpshashkin.dto.ConversionRequest;
import com.example.cerpshashkin.dto.ConversionResponse;
import com.example.cerpshashkin.service.CurrencyService;
import com.example.cerpshashkin.validation.ValidCurrency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
    private static final String LOG_DELETE_CURRENCY = "DELETE /api/v1/currencies/{} - removing currency";
    private static final String LOG_ADDED_SUCCESSFULLY = "Currency added: {}";
    private static final String LOG_REMOVED_SUCCESSFULLY = "Currency removed: {}";
    private static final String LOG_CONVERSION_REQUEST = "GET /api/v1/currencies/exchange-rates - converting {} {} to {}";
    private static final String LOG_CONVERSION_SUCCESS = "Conversion successful: {} {} = {} {}";
    private static final String LOG_REFRESH_RATES = "POST /api/v1/currencies/refresh - refreshing exchange rates";
    private static final String LOG_RATES_REFRESHED = "Exchange rates refreshed successfully";

    private static final String MESSAGE_CURRENCY_ADDED = "Currency %s added successfully";
    private static final String MESSAGE_CURRENCY_REMOVED = "Currency %s removed successfully";
    private static final String MESSAGE_RATES_UPDATED = "Exchange rates updated successfully";

    private static final String MESSAGE_CURRENCY_PARAM_REQUIRED = "Currency parameter is required";
    private static final String MESSAGE_CURRENCY_PATH_REQUIRED = "Currency path variable is required";

    private final CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<List<String>> getCurrencies() {
        log.info(LOG_GET_CURRENCIES);
        List<String> currencies = currencyService.getSupportedCurrencies();
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

        String message = String.format(MESSAGE_CURRENCY_ADDED, currency.toUpperCase());
        log.info(LOG_ADDED_SUCCESSFULLY, currency);

        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{currency}")
    @Validated
    public ResponseEntity<String> deleteCurrency(
            @PathVariable
            @NotBlank(message = MESSAGE_CURRENCY_PATH_REQUIRED)
            @ValidCurrency
            final String currency) {

        log.info(LOG_DELETE_CURRENCY, currency);
        currencyService.removeCurrency(currency);

        String message = String.format(MESSAGE_CURRENCY_REMOVED, currency.toUpperCase());
        log.info(LOG_REMOVED_SUCCESSFULLY, currency);

        return ResponseEntity.ok(message);
    }

    @GetMapping("/exchange-rates")
    public ResponseEntity<ConversionResponse> getExchangeRates(
            @Valid @ModelAttribute final ConversionRequest request) {

        log.info(LOG_CONVERSION_REQUEST, request.amount(), request.from(), request.to());

        ConversionResponse response = currencyService.convertCurrency(request);

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
}
