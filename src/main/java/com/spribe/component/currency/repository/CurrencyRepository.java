package com.spribe.component.currency.repository;

import com.spribe.component.currency.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByCode(String code);

    @Query("SELECT c FROM Currency c")
    List<Currency> findAllCurrencies();

    default Map<String, Currency> findCurrenciesAsMap() {
        return findAllCurrencies().stream()
                .collect(Collectors.toMap(Currency::getCode, currency -> currency));
    }
}
