package com.example.cerpshashkin.repository;

import com.example.cerpshashkin.entity.ExchangeRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, UUID> {

    Optional<ExchangeRateEntity> findFirstByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc(
            String baseCurrency,
            String targetCurrency
    );

    @Query("""
            SELECT e FROM ExchangeRateEntity e
            WHERE e.baseCurrency = :baseCurrency
              AND e.targetCurrency = :targetCurrency
              AND e.timestamp >= :startDate
              AND e.timestamp <= :endDate
            ORDER BY e.timestamp ASC
            """)
    List<ExchangeRateEntity> findRatesForPeriod(
            @Param("baseCurrency") String baseCurrency,
            @Param("targetCurrency") String targetCurrency,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Modifying
    @Query("DELETE FROM ExchangeRateEntity e WHERE e.timestamp < :cutoffDate")
    int deleteByTimestampBefore(@Param("cutoffDate") Instant cutoffDate);
}
