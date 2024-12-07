package com.spribe.currency.repositories;

import com.spribe.currency.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    boolean existsByName(String name);

    Optional<Currency> findByName(String name);
}
