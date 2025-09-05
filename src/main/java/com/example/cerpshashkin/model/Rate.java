package com.example.cerpshashkin.model;

import java.math.BigDecimal;

public record Rate(CurrencyEnum currency, BigDecimal value) {
}