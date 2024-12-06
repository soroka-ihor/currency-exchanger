package com.spribe.component.currency.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spribe.component.currency.dto.AddCurrenciesRequest;
import com.spribe.component.currency.dto.AddCurrenciesResponse;
import com.spribe.component.currency.service.CurrencyService;
import com.spribe.component.rate.dto.ExchangeRateResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FixerCurrencyController.class)
@AutoConfigureMockMvc(addFilters = false)
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetExchangeRates() throws Exception {
        when(service.getExchangeRatesByCurrency("USD", 100.0))
                .thenReturn(new ExchangeRateResponse("USD", LocalDateTime.now(), List.of()));
        mockMvc.perform(
                        get("/currency")
                        .param("currency", "USD")
                        .param("amount", "100.0")
                )
                .andExpect(status().isOk());
    }

    @Test
    void testAddCurrency() throws Exception {
        AddCurrenciesRequest request = new AddCurrenciesRequest();
        request.setCurrencyCodes(List.of("USD", "EUR"));
        when(service.addCurrencies(request)).thenReturn(new AddCurrenciesResponse("Currencies added successfully"));
        mockMvc.perform(post("/currency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Currencies added successfully"));
        verify(service, times(1)).addCurrencies(request);
    }
}


