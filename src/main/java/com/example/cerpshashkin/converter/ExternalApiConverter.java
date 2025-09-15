package com.example.cerpshashkin.converter;

import com.example.cerpshashkin.dto.ExchangeRatesApiResponse;
import com.example.cerpshashkin.dto.FixerioResponse;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExternalApiConverter {

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
