package com.example.cerpshashkin.service;

import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

@Service
@Slf4j
public class MockCurrencyService {

    public CurrencyExchangeResponse getExchangeRates() {
        log.info("Class MockCurrencyService method getExchangeRates");

        Map<Currency, BigDecimal> rates = Map.of(
                Currency.getInstance("CAD"), BigDecimal.valueOf(1.260046),
                Currency.getInstance("CHF"), BigDecimal.valueOf(0.933058),
                Currency.getInstance("EUR"), BigDecimal.valueOf(0.806942),
                Currency.getInstance("GBP"), BigDecimal.valueOf(0.719154),
                Currency.getInstance("JPY"), BigDecimal.valueOf(107.346001),
                Currency.getInstance("AUD"), BigDecimal.valueOf(1.355018)
        );

        return CurrencyExchangeResponse.success(
                Currency.getInstance("USD"),
                LocalDate.now(),
                rates
        );
    }
}
