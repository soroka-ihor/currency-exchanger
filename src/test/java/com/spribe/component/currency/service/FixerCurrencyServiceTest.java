package com.spribe.component.currency.service;

import com.spribe.component.currency.dto.AddCurrenciesRequest;
import com.spribe.component.currency.dto.AddCurrenciesResponse;
import com.spribe.component.currency.model.Currency;
import com.spribe.component.currency.repository.CurrencyRepository;
import com.spribe.component.currency.service.impl.FixerCurrencyService;
import com.spribe.component.rate.dto.Rate;
import com.spribe.exception.model.InvalidCurrencyCodeException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FixerCurrencyServiceTest {

    @Mock
    private MockWebServer mockWebServer;

    @Mock
    private CurrencyRepository repository;

    @InjectMocks
    private FixerCurrencyService fixerCurrencyService;

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Set up the WebClient to use the MockWebServer's URL
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        fixerCurrencyService = new FixerCurrencyService(repository);
        //fixerCurrencyService.webClient = webClient;
        Field webClientField = FixerCurrencyService.class.getDeclaredField("webClient");
        webClientField.setAccessible(true);
        webClientField.set(fixerCurrencyService, webClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void testAddCurrencies_Success() throws Exception {
        // Mock repository behavior
        when(repository.findByCode(anyString())).thenReturn(Optional.empty());

        // Mock server response
        String validResponseJson = """
                {
                    "success": true,
                    "rates": {
                        "GBP": 0.85,
                        "USD": 1.1
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(validResponseJson)
                .addHeader("Content-Type", "application/json"));

        // Mock request
        var request = new AddCurrenciesRequest(List.of("USD,GBP"));

        // Call the method
        AddCurrenciesResponse response = fixerCurrencyService.addCurrencies(request);

        // Verify interactions and assertions
        verify(repository, times(2)).save(any(Currency.class));
        assertEquals("Currencies GBP,USD were added successfully.", response.getMessage());
    }

    @Test
    void testAddCurrencies_InvalidCurrencyCodes() {
        // Mock server response
        String invalidResponseJson = """
                {
                    "success": false,
                    "error": {
                        "code": 101,
                        "info": "Invalid currency codes"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(invalidResponseJson)
                .addHeader("Content-Type", "application/json"));

        // Call the method and expect an exception
        Exception exception = assertThrows(InvalidCurrencyCodeException.class, () -> {
            fixerCurrencyService.addCurrencies(new AddCurrenciesRequest(List.of("INVALID")));
        });

        // Verify the exception message
        assertEquals("You have provided one or more invalid Currency Codes. [Required format: currencies=EUR,USD,GBP,...]", exception.getMessage());
    }
}
