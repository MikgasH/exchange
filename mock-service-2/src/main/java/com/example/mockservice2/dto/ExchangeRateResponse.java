package com.example.mockservice2.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Builder
public record ExchangeRateResponse(
        boolean success,
        long timestamp,
        String base,
        LocalDate date,
        Map<String, BigDecimal> rates
) {}
