package com.spribe.component.currency.service.impl;

import com.spribe.component.currency.dto.AddCurrenciesRequest;
import com.spribe.component.currency.dto.AddCurrenciesResponse;
import com.spribe.component.currency.model.Currency;
import com.spribe.component.currency.repository.CurrencyRepository;
import com.spribe.component.currency.service.CurrencyService;
import com.spribe.component.rate.dto.ExchangeRateResponse;
import com.spribe.component.rate.dto.Rate;
import com.spribe.exception.model.InvalidCurrencyCodeException;
import com.spribe.util.BigDecimalConverter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FixerCurrencyService implements CurrencyService {

    @Value("${fixer.api.key}")
    private String apiKey;

    private final CurrencyRepository repository;
    private final WebClient webClient;

    public FixerCurrencyService(CurrencyRepository repository) {
        this.repository = repository;
        this.webClient = WebClient.builder().baseUrl("http://data.fixer.io").build();
    }

    // Rates with EUR as base,
    // since free plan at fixer.io
    // provides only EUR as base currency
    private final Map<String, BigDecimal> exchangeRates = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // fill exchange rates map from database
        var existingCurrencies = repository.findAll();
        existingCurrencies.forEach(
                currency -> exchangeRates.put(currency.getCode(), currency.getRateToBaseCurrency())
        );
    }
    /**
     * Save currency to the database.
     * @param currencyCodes of the currency (e.g. USD).
     * @return
     */
    @Override
    @Transactional
    public AddCurrenciesResponse addCurrencies(AddCurrenciesRequest request) {
        var currencyCodes = String.join(",", request.getCurrencyCodes());
        validateCurrencyCodesAndThrowIfFail(currencyCodes);
        var responseFromFixer = getLatestRates(currencyCodes);
        Set<Rate> rates = null;
        if (success(responseFromFixer)) {
            rates = parseRates(responseFromFixer);
            rates.forEach(
                    rate -> {
                        checkIfExists(rate.getCurrencyCode(), rate.getRate());
                        exchangeRates.put(rate.getCurrencyCode(), rate.getRate());
                    }
            );
        } else {
            throw new InvalidCurrencyCodeException();
        }
        return new AddCurrenciesResponse(
                String.format(
                        "Currencies %s were added successfully.",
                        rates.stream().map(Rate::getCurrencyCode).collect(Collectors.joining(","))
                )
        );
    }

    private void checkIfExists(String currencyCode, BigDecimal baseCurrencyExchangeRate) {
        var currencyOpt = repository.findByCode(currencyCode);
        if (currencyOpt.isPresent()) {
            update(currencyOpt, baseCurrencyExchangeRate);
        }
        if (currencyOpt.isEmpty()) {
            createAndSave(currencyCode, baseCurrencyExchangeRate);
        }
    }

    private void createAndSave(String currencyCode, BigDecimal baseCurrencyExchangeRate) {
        var currency = new Currency();
        currency.setCode(currencyCode);
        currency.setRateToBaseCurrency(baseCurrencyExchangeRate);
        repository.save(currency);
    }

    private void update(Optional<Currency> currencyOpt, BigDecimal baseCurrencyExchangeRate) {
        currencyOpt.get().setRateToBaseCurrency(baseCurrencyExchangeRate);
        repository.save(currencyOpt.get());
    }

    /**
     * Calculate base currency rate to the existing currencies.
     * @param baseCurrencyCode of the base currency.
     * @return
     */
    @Override
    public ExchangeRateResponse getExchangeRatesByCurrency(String baseCurrencyCode, double amount) {
        // validate base currency code
        // check if the requested currency is supported
        if (!exchangeRates.containsKey(baseCurrencyCode.toUpperCase())) {
            throw new RuntimeException("Currency code is not supported: " + baseCurrencyCode);
        }
        List<Rate> rates = new ArrayList<>();
        exchangeRates.forEach((currencyCode, rate) -> {
            if (!currencyCode.equals(baseCurrencyCode)) {
                rates.add(new Rate(
                        currencyCode,
                        convert(BigDecimal.valueOf(amount), baseCurrencyCode, currencyCode)
                ));
            }
        });
        return new ExchangeRateResponse(
                baseCurrencyCode,
                LocalDateTime.now(),
                rates
        );
    }

    /**
     * Since fixer.io provides only EUR as a base currency,
     * we need such method to enable converting from any currency
     * to another using a single base currency.
     * @param amount
     * @param fromCurrencyCode
     * @param toCurrencyCode
     * @return
     */
    public BigDecimal convert(BigDecimal amount, String fromCurrencyCode, String toCurrencyCode) {
        if (fromCurrencyCode.equalsIgnoreCase(toCurrencyCode)) {
            return amount;
        }

        var fromRate = exchangeRates.get(fromCurrencyCode.toUpperCase());
        var toRate = exchangeRates.get(toCurrencyCode.toUpperCase());

        if (fromRate == null || toRate == null) {
            throw new IllegalArgumentException("Unsupported currency: " +
                    (fromRate == null ? fromCurrencyCode : toCurrencyCode));
        }

        // Converting using the formula: amount * (toRate / fromRate)
        return amount.multiply(toRate).divide(fromRate, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Fetch latest exchange rates for specific symbols.
     *
     * @param symbols Comma-separated currency codes (e.g., "GBP,JPY,EUR")
     * @return Map containing exchange rates
     */
    public Map<String, Object> getLatestRates(String symbols) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/latest")
                        .queryParam("access_key", apiKey)
                        .queryParam("symbols", symbols)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    @Override
    @Transactional
    public void updateExchangeRatesForAllCurrencies() {
        var updatedAt = LocalDateTime.now();
        var allCurrencies = repository.findCurrenciesAsMap();
        var currencyCodes = String.join(",", allCurrencies.keySet());
        var responseFromFixer = getLatestRates(currencyCodes);

        if (success(responseFromFixer)) {
            parseRates(responseFromFixer).forEach(
                    rate -> {
                        if (allCurrencies.containsKey(rate.getCurrencyCode())) {
                            allCurrencies.get(rate.getCurrencyCode()).setRateToBaseCurrency(rate.getRate());
                            allCurrencies.get(rate.getCurrencyCode()).setUpdatedAt(updatedAt);
                        }
                    }
            );
        } else {
            throw new InvalidCurrencyCodeException();
        }
        repository.saveAll(allCurrencies.values());
    }

    @Override
    public String getSupportedCurrencies() {
        return String.join(",", exchangeRates.keySet());
    }

    private void validateCurrencyCodesAndThrowIfFail(String currencyCodes) {
        // validation logic
    }

    private boolean success(Map<String, Object> responseFromFixer) {
        return responseFromFixer.containsKey("success") && responseFromFixer.get("success").equals(Boolean.TRUE);
    }

    private Set<Rate> parseRates(Map<String, Object> responseFromFixer) {
        var rates = new HashSet<Rate>();
        ((LinkedHashMap) responseFromFixer.get("rates")).forEach(
                (code, rate) ->
                    rates.add(new Rate(
                            String.valueOf(code),
                            BigDecimalConverter.toBigDecimal(rate)
                    ))
        );
        return rates;
    }

}
