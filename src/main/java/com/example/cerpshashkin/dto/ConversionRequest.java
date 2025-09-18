package com.example.cerpshashkin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ConversionRequest(
        @NotNull
        @Positive
        BigDecimal amount,

        @NotBlank
        @Size(min = 3, max = 3, message = ConversionRequest.CURRENCY_SIZE_MESSAGE)
        String from,

        @NotBlank
        @Size(min = 3, max = 3, message = ConversionRequest.CURRENCY_SIZE_MESSAGE)
        String to
) {
    public static final String CURRENCY_SIZE_MESSAGE = "Currency code must be exactly 3 characters";
}
