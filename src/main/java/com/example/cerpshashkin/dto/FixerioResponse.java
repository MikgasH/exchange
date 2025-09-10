package com.example.cerpshashkin.dto;

import com.example.cerpshashkin.converter.ResponseConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

/**
 * DTO for Fixer.io API response with automatic JSON conversion.
 */
public record FixerioResponse(
        boolean success,

        @JsonProperty("timestamp")
        @JsonDeserialize(using = ResponseConverter.TimestampToInstantDeserializer.class)
        Instant lastUpdated,

        @JsonDeserialize(using = ResponseConverter.CurrencyDeserializer.class)
        Currency base,

        @JsonProperty("date")
        LocalDate rateDate,

        @JsonDeserialize(keyUsing = ResponseConverter.CurrencyKeyDeserializer.class)
        Map<Currency, BigDecimal> rates
) {}
