package com.example.cerpshashkin.service;

import com.example.cerpshashkin.model.CurrencyEnum;
import com.example.cerpshashkin.model.CurrencyExchangeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.cerpshashkin.model.Rate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class MockCurrencyService {

    public CurrencyExchangeResponse getExchangeRates() {
        log.info("Class MockCurrencyService method getExchangeRates");

        List<Rate> rates = List.of(
                new Rate(CurrencyEnum.CAD, BigDecimal.valueOf(1.260046)),
                new Rate(CurrencyEnum.CHF, BigDecimal.valueOf(0.933058)),
                new Rate(CurrencyEnum.EUR, BigDecimal.valueOf(0.806942)),
                new Rate(CurrencyEnum.GBP, BigDecimal.valueOf(0.719154)),
                new Rate(CurrencyEnum.JPY, BigDecimal.valueOf(107.346001)),
                new Rate(CurrencyEnum.AUD, BigDecimal.valueOf(1.355018))
        );

        return CurrencyExchangeResponse.success(CurrencyEnum.USD, LocalDate.now(), rates);
    }

    public List<String> getSupportedCurrencies() {
        log.info("Class MockCurrencyService method getSupportedCurrencies");

        return List.of();
    }
}
