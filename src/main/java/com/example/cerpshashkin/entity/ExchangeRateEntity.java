package com.example.cerpshashkin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exchange_rates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExchangeRateEntity {

    public static final String SOURCE_AGGREGATED = "AGGREGATED";
    public static final String SOURCE_MOCK = "MOCK";

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "target_currency", nullable = false, length = 3)
    private String targetCurrency;

    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal rate;

    @Column(length = 20)
    private String source;

    @Column(nullable = false)
    private Instant timestamp;
}
