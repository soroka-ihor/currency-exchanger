package com.spribe.component.currency.service;

import com.spribe.component.currency.dto.AddCurrenciesRequest;
import com.spribe.component.currency.dto.AddCurrenciesResponse;
import com.spribe.component.rate.dto.ExchangeRateResponse;

public interface CurrencyService {
    String getSupportedCurrencies();
    AddCurrenciesResponse addCurrencies(AddCurrenciesRequest request);
    ExchangeRateResponse getExchangeRatesByCurrency(String code, double amount);
    void updateExchangeRatesForAllCurrencies();
}
