package com.spribe.currency.service;

import com.spribe.currency.dto.ExchangeRateIntegrationResponse;
import com.spribe.currency.dto.ExchangeRateResponse;
import com.spribe.currency.entity.Currency;
import com.spribe.currency.entity.ExchangeRate;
import com.spribe.currency.integration.ExchangeRateIntegration;
import com.spribe.currency.mapper.ExchangeRateMapper;
import com.spribe.currency.repositories.CurrencyRepository;
import com.spribe.currency.repositories.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateMapper exchangeRateMapper;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private ExchangeRateIntegration exchangeRateIntegration;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    void getExchangeRatesCache_shouldReturnMappedResponses() throws NoSuchFieldException, IllegalAccessException {
        // given
        String currency = "USD";
        ExchangeRate rate1 = new ExchangeRate();
        ExchangeRate rate2 = new ExchangeRate();
        List<ExchangeRate> rates = List.of(rate1, rate2);

        ExchangeRateService spyService = spy(exchangeRateService);

        Field field = ExchangeRateService.class.getDeclaredField("exchangeRatesCache");
        field.setAccessible(true);

        Map<String, List<ExchangeRate>> mockCache = mock(Map.class);
        field.set(spyService, mockCache);

        when(mockCache.get(currency)).thenReturn(rates);

        ExchangeRateResponse response1 = new ExchangeRateResponse("USD",
                                                                  "EUR",
                                                                  BigDecimal.valueOf(0.84));
        ExchangeRateResponse response2 = new ExchangeRateResponse("USD",
                                                                  "GBP",
                                                                  BigDecimal.valueOf(0.75));
        when(exchangeRateMapper.toResponses(rates)).thenReturn(List.of(response1, response2));

        // when
        List<ExchangeRateResponse> result = spyService.getExchangeRates(currency);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(response1, result.get(0));
        assertEquals(response2, result.get(1));

        verify(mockCache).get(currency);
        verify(exchangeRateMapper).toResponses(rates);
    }

    @Test
    void fetchCurrencyExchangeRates_shouldFetchAndSaveRatesForTwoCurrencies() throws NoSuchFieldException, IllegalAccessException {
        // given
        BigDecimal exchangeRate = BigDecimal.valueOf(0.84);
        BigDecimal reverseExchangeRate = BigDecimal.ONE.divide(exchangeRate, RoundingMode.HALF_UP);

        Currency baseCurrency = new Currency(1L, "USD");
        Currency targetCurrency = new Currency(2L, "EUR");
        List<Currency> allCurrencies = List.of(targetCurrency);

        when(currencyRepository.findAll()).thenReturn(allCurrencies);

        ExchangeRateIntegrationResponse integrationResponse = new ExchangeRateIntegrationResponse(
                true,
                "terms",
                "privacy",
                1L,
                "USD",
                Map.of("USDEUR", exchangeRate)
        );
        when(exchangeRateIntegration.getExchangeRates(Set.of("EUR"), "USD")).thenReturn(integrationResponse);

        when(exchangeRateMapper.mapToEntity(eq(baseCurrency), eq(targetCurrency), eq(exchangeRate), eq(1L)))
                .thenReturn(new ExchangeRate(null,
                                             baseCurrency,
                                             targetCurrency,
                                             exchangeRate,
                                             1L));

        when(exchangeRateMapper.mapToEntity(eq(targetCurrency), eq(baseCurrency), eq(reverseExchangeRate), eq(1L)))
                .thenReturn(new ExchangeRate(null,
                                             targetCurrency,
                                             baseCurrency,
                                             reverseExchangeRate,
                                             1L));

        ExchangeRateService spyService = spy(exchangeRateService);

        // when
        spyService.fetchCurrencyExchangeRates(baseCurrency);

        // then
        verify(currencyRepository).findAll();
        verify(exchangeRateIntegration, times(1)).getExchangeRates(Set.of("EUR"), "USD");
        verify(exchangeRateRepository, times(1)).saveAll(anyList());

        Field field = ExchangeRateService.class.getDeclaredField("exchangeRatesCache");
        field.setAccessible(true);
        Map<String, List<ExchangeRate>> cache = (Map<String, List<ExchangeRate>>) field.get(spyService);

        assertNotNull(cache);
        assertTrue(cache.containsKey("USD"));
        assertTrue(cache.containsKey("EUR"));
        assertEquals(1, cache.get("USD").size());
        assertEquals(1, cache.get("EUR").size());
        assertEquals(exchangeRate, cache.get("USD").get(0).getRate());
        assertEquals(reverseExchangeRate, cache.get("EUR").get(0).getRate());
    }

    @Test
    void fetchCurrencyExchangeRates_shouldFetchAndSaveRates() {
        // given
        BigDecimal exchangeRate = BigDecimal.valueOf(0.84);
        Currency baseCurrency = new Currency(1L, "USD");
        Currency targetCurrency = new Currency(2L, "EUR");
        List<Currency> allCurrencies = List.of(targetCurrency);

        when(currencyRepository.findAll()).thenReturn(allCurrencies);

        ExchangeRateIntegrationResponse integrationResponse = new ExchangeRateIntegrationResponse(
                true,
                "terms",
                "privacy",
                1L,
                "USD",
                Map.of("USDEUR", exchangeRate)
        );
        when(exchangeRateIntegration.getExchangeRates(Set.of("EUR"), "USD")).thenReturn(integrationResponse);

        when(exchangeRateMapper.mapToEntity(
                eq(baseCurrency),
                eq(targetCurrency),
                eq(exchangeRate),
                eq(1L)
        )).thenReturn(new ExchangeRate(null,
                                       baseCurrency,
                                       targetCurrency,
                                       exchangeRate,
                                       1L)
        );

        when(exchangeRateMapper.mapToEntity(
                eq(targetCurrency),
                eq(baseCurrency),
                any(BigDecimal.class),
                eq(1L)
        )).thenReturn(new ExchangeRate(null,
                                       targetCurrency,
                                       baseCurrency,
                                       exchangeRate,
                                       1L)
        );

        // when
        exchangeRateService.fetchCurrencyExchangeRates(baseCurrency);

        // then
        verify(currencyRepository).findAll();
        verify(exchangeRateIntegration).getExchangeRates(Set.of("EUR"), "USD");
        verify(exchangeRateRepository, times(1)).saveAll(anyList());

        List<ExchangeRateResponse> result = exchangeRateService.getExchangeRates("USD");
        assertNotNull(result);
    }

    @Test
    void updateCurrencyExchangeRates_shouldUpdateExistingRates() {
        // given
        double rate = 0.84;
        double reversedRate = 1.190476;
        Currency baseCurrency = new Currency(1L, "USD");
        Currency targetCurrency = new Currency(2L, "EUR");
        List<Currency> allCurrencies = List.of(baseCurrency, targetCurrency);
        when(currencyRepository.findAll()).thenReturn(allCurrencies);

        ExchangeRateIntegrationResponse integrationUDSResponse = new ExchangeRateIntegrationResponse(
                true,
                "terms",
                "privacy",
                1L,
                "USD",
                Map.of("USDEUR", BigDecimal.valueOf(rate))
        );
        when(exchangeRateIntegration.getExchangeRates(Set.of("EUR"), "USD")).thenReturn(integrationUDSResponse);

        ExchangeRateIntegrationResponse integrationEURResponse = new ExchangeRateIntegrationResponse(
                true,
                "terms",
                "privacy",
                1L,
                "EUR",
                Map.of("EURUSD", BigDecimal.valueOf(rate))
        );

        ExchangeRate existingRate = new ExchangeRate(
                1L,
                baseCurrency,
                targetCurrency,
                BigDecimal.valueOf(rate),
                1L
        );
        List<ExchangeRate> dbRates = List.of(existingRate);
        when(exchangeRateRepository.findByBaseOrTargetCurrency(baseCurrency)).thenReturn(dbRates);

        ExchangeRate fetchedUSDRate = new ExchangeRate(
                1L,
                baseCurrency,
                targetCurrency,
                BigDecimal.valueOf(rate),
                1L
        );
        ExchangeRate fetchedEURRate = new ExchangeRate(
                1L,
                targetCurrency,
                baseCurrency,
                BigDecimal.valueOf(reversedRate),
                1L
        );

        when(exchangeRateMapper.mapToEntity(
                eq(baseCurrency),
                eq(targetCurrency),
                eq(BigDecimal.valueOf(rate)),
                eq(integrationUDSResponse.timestamp())))
                .thenReturn(fetchedUSDRate);

        when(exchangeRateMapper.mapToEntity(
                eq(targetCurrency),
                eq(baseCurrency),
                eq(BigDecimal.ONE.divide(BigDecimal.valueOf(rate), RoundingMode.HALF_UP)),
                eq(integrationEURResponse.timestamp())))
                .thenReturn(fetchedEURRate);

        // when
        exchangeRateService.updateCurrencyExchangeRates(baseCurrency);

        // then
        verify(exchangeRateRepository).findByBaseOrTargetCurrency(baseCurrency);

        assertEquals(BigDecimal.valueOf(rate), existingRate.getRate());
        assertNotNull(existingRate.getUpdated());
    }

    @Test
    void fetchCurrencyExchangeRates_shouldSkipIfNoCurrencies() {
        // given
        Currency baseCurrency = new Currency(1L, "USD");
        when(currencyRepository.findAll()).thenReturn(List.of(baseCurrency));

        // when
        exchangeRateService.fetchCurrencyExchangeRates(baseCurrency);

        // then
        verify(currencyRepository).findAll();
        verifyNoInteractions(exchangeRateIntegration, exchangeRateRepository);
    }
}
