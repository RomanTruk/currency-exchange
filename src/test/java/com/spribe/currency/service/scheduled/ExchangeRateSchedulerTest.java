package com.spribe.currency.service.scheduled;

import com.spribe.currency.entity.Currency;
import com.spribe.currency.repositories.CurrencyRepository;
import com.spribe.currency.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateSchedulerTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private ExchangeRateScheduler exchangeRateScheduler;

    @Captor
    private ArgumentCaptor<Currency> currencyCaptor;

    @Test
    void testFetchAndUpdateExchangeRates_isOk() {
        // given
        Currency currency1 = new Currency(1L, "USD");
        Currency currency2 = new Currency(2L, "EUR");
        when(currencyRepository.findAll()).thenReturn(List.of(currency1, currency2));

        doNothing().when(exchangeRateService).updateCurrencyExchangeRates(any(Currency.class));

        // when
        exchangeRateScheduler.fetchAndUpdateExchangeRates();

        // then
        verify(currencyRepository, times(1)).findAll();
        verify(exchangeRateService, times(2)).updateCurrencyExchangeRates(currencyCaptor.capture());

        List<Currency> capturedCurrencies = currencyCaptor.getAllValues();
        assert capturedCurrencies.contains(currency1);
        assert capturedCurrencies.contains(currency2);
    }

    @Test
    void testFetchAndUpdateExchangeRates_IfCurrenciesNotFound() {
        // given
        when(currencyRepository.findAll()).thenReturn(List.of());

        // when
        exchangeRateScheduler.fetchAndUpdateExchangeRates();

        // then
        verify(currencyRepository, times(1)).findAll();
        verifyNoInteractions(exchangeRateService);
    }

    @Test
    void testFetchAndUpdateExchangeRates_ThrowExceptionInServiceForOneCurrency() {
        // given
        Currency currency1 = new Currency(1L, "USD");
        Currency currency2 = new Currency(2L, "EUR");
        when(currencyRepository.findAll()).thenReturn(List.of(currency1, currency2));

        doThrow(new RuntimeException("Service failure"))
                .when(exchangeRateService).updateCurrencyExchangeRates(currency1);
        doNothing().when(exchangeRateService).updateCurrencyExchangeRates(currency2);

        // when
        exchangeRateScheduler.fetchAndUpdateExchangeRates();

        // then
        verify(currencyRepository, times(1)).findAll();
        verify(exchangeRateService, times(2)).updateCurrencyExchangeRates(currencyCaptor.capture());

        List<Currency> capturedCurrencies = currencyCaptor.getAllValues();
        assert capturedCurrencies.contains(currency1);
        assert capturedCurrencies.contains(currency2);

        verify(exchangeRateService).updateCurrencyExchangeRates(currency2);
    }
}
