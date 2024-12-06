package com.spribe.component.currency.controller;

import com.spribe.component.currency.dto.AddCurrenciesRequest;
import com.spribe.component.currency.service.CurrencyService;
import com.spribe.component.rate.dto.ExchangeRateResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/currency")
public class FixerCurrencyController {

    private final CurrencyService service;

    @Operation(description = "Gets exchange rate for passed currency towards all supported currencies.")
    @GetMapping
    public ResponseEntity<ExchangeRateResponse> getExchangeRates(
            @RequestParam String currency,
            @RequestParam double amount
    ) {
        return ResponseEntity.ok(service.getExchangeRatesByCurrency(currency, amount));
    }

    @Operation(description = "Adds currencies. Input validation mostly omitted.")
    @PostMapping
    public ResponseEntity<String> addCurrency(
            @RequestBody AddCurrenciesRequest request
    ) {
        return ResponseEntity.ok(service.addCurrencies(request).getMessage());
    }

    @Operation(description = "Fetches all supported currencies.")
    @GetMapping("/all")
    public ResponseEntity<String> getAllUsedCurrencies() {
        return ResponseEntity.ok(service.getSupportedCurrencies());
    }
}
