package com.example.cerpshashkin.converter;

import com.example.cerpshashkin.dto.ExchangeRatesApiResponse;
import com.example.cerpshashkin.dto.FixerioResponse;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Converter for transforming external API responses to unified CurrencyExchangeResponse.
 */
@Component
@Slf4j
public class ExternalApiConverter {

    /**
     * Converts Fixer.io response to CurrencyExchangeResponse format.
     *
     * @param fixerResponse the response from Fixer.io API
     * @return unified CurrencyExchangeResponse
     * @throws IllegalArgumentException if fixerResponse is null
     */
    public CurrencyExchangeResponse convertFromFixer(final FixerioResponse fixerResponse) {
        if (fixerResponse == null) {
            throw new IllegalArgumentException("FixerioResponse cannot be null");
        }

        if (!fixerResponse.success()) {
            log.warn("Fixer.io API returned unsuccessful response");
            return CurrencyExchangeResponse.failure();
        }

        return new CurrencyExchangeResponse(
                true,
                fixerResponse.lastUpdated(),
                fixerResponse.base(),
                fixerResponse.rateDate(),
                fixerResponse.rates()
        );
    }

    /**
     * Converts ExchangeRatesAPI response to CurrencyExchangeResponse format.
     *
     * @param exchangeRatesResponse the response from ExchangeRatesAPI
     * @return unified CurrencyExchangeResponse
     * @throws IllegalArgumentException if exchangeRatesResponse is null
     */
    public CurrencyExchangeResponse convertFromExchangeRates(final ExchangeRatesApiResponse exchangeRatesResponse) {
        if (exchangeRatesResponse == null) {
            throw new IllegalArgumentException("ExchangeRatesApiResponse cannot be null");
        }

        if (!exchangeRatesResponse.success()) {
            log.warn("ExchangeRatesAPI returned unsuccessful response");
            return CurrencyExchangeResponse.failure();
        }

        return new CurrencyExchangeResponse(
                true,
                exchangeRatesResponse.lastUpdated(),
                exchangeRatesResponse.base(),
                exchangeRatesResponse.rateDate(),
                exchangeRatesResponse.rates()
        );
    }
}
