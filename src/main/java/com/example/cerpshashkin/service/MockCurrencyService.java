package com.example.cerpshashkin.service;

import com.example.cerpshashkin.model.CurrencyEnum;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;

/**
 * Mock implementation of currency exchange service.
 */
@Service
@Slf4j
public class MockCurrencyService {

    /** Exchange rate for Canadian Dollar. */
    private static final BigDecimal CAD_RATE = BigDecimal.valueOf(1.260046);
    /** Exchange rate for Swiss Franc. */
    private static final BigDecimal CHF_RATE = BigDecimal.valueOf(0.933058);
    /** Exchange rate for Euro. */
    private static final BigDecimal EUR_RATE = BigDecimal.valueOf(0.806942);
    /** Exchange rate for British Pound. */
    private static final BigDecimal GBP_RATE = BigDecimal.valueOf(0.719154);
    /** Exchange rate for Japanese Yen. */
    private static final BigDecimal JPY_RATE = BigDecimal.valueOf(107.346001);
    /** Exchange rate for Australian Dollar. */
    private static final BigDecimal AUD_RATE = BigDecimal.valueOf(1.355018);

    /**
     * Gets current exchange rates for supported currencies.
     */
    public CurrencyExchangeResponse getExchangeRates() {
        log.info("Class MockCurrencyService method getExchangeRates");

        EnumMap<CurrencyEnum, BigDecimal> rates = new EnumMap<>(CurrencyEnum.class);
        rates.put(CurrencyEnum.CAD, CAD_RATE);
        rates.put(CurrencyEnum.CHF, CHF_RATE);
        rates.put(CurrencyEnum.EUR, EUR_RATE);
        rates.put(CurrencyEnum.GBP, GBP_RATE);
        rates.put(CurrencyEnum.JPY, JPY_RATE);
        rates.put(CurrencyEnum.AUD, AUD_RATE);

        return CurrencyExchangeResponse.success(CurrencyEnum.USD, LocalDate.now(), rates);
    }

    /**
     * Gets list of supported currencies.
     */
    public List<String> getSupportedCurrencies() {
        log.info("Class MockCurrencyService method getSupportedCurrencies");

        return List.of();
    }
}
