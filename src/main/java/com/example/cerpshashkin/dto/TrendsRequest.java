package com.example.cerpshashkin.dto;

import com.example.cerpshashkin.validation.ValidCurrency;
import com.example.cerpshashkin.validation.ValidPeriod;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TrendsRequest(
        @ValidCurrency(message = "Invalid source currency code")
        @NotBlank(message = "Source currency is required")
        String from,

        @ValidCurrency(message = "Invalid target currency code")
        @NotBlank(message = "Target currency is required")
        String to,

        @ValidPeriod(message = "Invalid period. Min: 12H/1D/1M/1Y, Max: 8760H/365D/12M/1Y. Examples: 12H, 7D, 3M, 1Y")
        @NotBlank(message = "Period is required")
        String period
) {}
