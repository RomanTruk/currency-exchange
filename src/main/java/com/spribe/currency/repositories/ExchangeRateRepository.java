package com.spribe.currency.repositories;

import com.spribe.currency.entity.Currency;
import com.spribe.currency.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    @Query("SELECT e FROM ExchangeRate e WHERE e.baseCurrency = :currency OR e.targetCurrency = :currency")
    List<ExchangeRate> findByBaseOrTargetCurrency(@Param("currency") Currency currency);
}
