package com.example.cerpshashkin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyExchangeResponse {
    private boolean success = true;
    private long timestamp = System.currentTimeMillis() / 1000;
    private String base;
    private String date;
    private Map<String, Double> rates;
}
