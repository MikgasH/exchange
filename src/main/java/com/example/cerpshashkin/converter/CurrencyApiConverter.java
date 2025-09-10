package com.example.cerpshashkin.converter;

import com.example.cerpshashkin.dto.CurrencyApiRawResponse;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

/**
 * Converter for CurrencyAPI (different JSON structure).
 */
@Component
@Slf4j
public class CurrencyApiConverter {

    /**
     * Converts CurrencyAPI response to CurrencyExchangeResponse format.
     */
    public CurrencyExchangeResponse convertToCurrencyExchange(CurrencyApiRawResponse raw) {
        if (raw == null || raw.data() == null) {
            return new CurrencyExchangeResponse(false,
                    java.time.Instant.now(), null, null, Map.of());
        }

        Map<Currency, BigDecimal> rates = new HashMap<>();

        raw.data().forEach((currencyCode, currencyData) -> {
            try {
                Currency currency = Currency.getInstance(currencyCode);
                rates.put(currency, currencyData.value());
            } catch (IllegalArgumentException e) {
                log.debug("Skipping unsupported currency: {}", currencyCode);
            }
        });

        return CurrencyExchangeResponse.success(
                Currency.getInstance("USD"),
                raw.meta().lastUpdatedAt().toLocalDate(),
                rates
        );
    }
}
