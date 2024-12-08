package com.spribe.currency.integration;

import com.spribe.currency.dto.ExchangeRateIntegrationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateIntegrationTest {

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @InjectMocks
    private ExchangeRateIntegration exchangeRateIntegration;

    @Test
    void getExchangeRates_shouldReturnSuccessfulResponse() {
        // given
        Set<String> currencies = Set.of("EUR", "GBP");
        String source = "USD";

        Map<String, BigDecimal> rates = Map.of("USDEUR", BigDecimal.valueOf(0.84),
                                               "USDGBP", BigDecimal.valueOf(0.75));
        ExchangeRateIntegrationResponse response = new ExchangeRateIntegrationResponse(true,
                                                                                       "terms",
                                                                                       "privacy",
                                                                                       1L,
                                                                                       "USD",
                                                                                       rates);

        ArgumentCaptor<String> currenciesCaptor = ArgumentCaptor.forClass(String.class);
        when(exchangeRateClient.getExchangeRates(eq(null), currenciesCaptor.capture(), eq(source), eq(0)))
                .thenReturn(response);

        // when
        ExchangeRateIntegrationResponse result = exchangeRateIntegration.getExchangeRates(currencies, source);

        // then
        assertNotNull(result);
        assertTrue(result.success());
        assertEquals(rates, result.rates());

        assertTrue(currenciesCaptor.getValue().contains("EUR"));
        assertTrue(currenciesCaptor.getValue().contains("GBP"));
        assertEquals(2, currenciesCaptor.getValue().split(",").length);
    }

    @Test
    void getExchangeRates_shouldThrowRuntimeException_whenResponseIsNull() {
        // given
        Set<String> currencies = Set.of("EUR");
        String source = "USD";

        when(exchangeRateClient.getExchangeRates(null, "EUR", source, 0))
                .thenReturn(null);

        // when - then
        RuntimeException exception = assertThrows(RuntimeException.class,
                                                  () -> exchangeRateIntegration.getExchangeRates(currencies, source));

        assertEquals("Failed to fetch exchange rates from API", exception.getMessage());
    }

    @Test
    void getExchangeRates_shouldThrowRuntimeException_whenSuccessIfFalse() {
        // given
        Set<String> currencies = Set.of("EUR");
        String source = "USD";

        when(exchangeRateClient.getExchangeRates(null, "EUR", source, 0))
                .thenReturn(new ExchangeRateIntegrationResponse(false,
                                                                null,
                                                                null,
                                                                0L,
                                                                null,
                                                                null));

        // when - then
        RuntimeException exception = assertThrows(RuntimeException.class,
                                                  () -> exchangeRateIntegration.getExchangeRates(currencies, source));

        assertEquals("Failed to fetch exchange rates from API", exception.getMessage());
    }
}
