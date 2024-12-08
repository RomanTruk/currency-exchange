package com.spribe.currency.service;

import com.spribe.currency.dto.CurrencyResponse;
import com.spribe.currency.entity.Currency;
import com.spribe.currency.mapper.CurrencyMapper;
import com.spribe.currency.repositories.CurrencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyMapper currencyMapper;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    void getCurrenciesWithPagination_shouldReturnPagedCurrencies() {
        // given
        int page = 0;
        int size = 2;
        Currency currency1 = new Currency(1L, "USD");
        Currency currency2 = new Currency(2L, "EUR");

        Page<Currency> currenciesPage = new PageImpl<>(List.of(currency1, currency2));
        when(currencyRepository.findAll(PageRequest.of(page, size))).thenReturn(currenciesPage);

        CurrencyResponse response1 = new CurrencyResponse("USD");
        CurrencyResponse response2 = new CurrencyResponse("EUR");
        when(currencyMapper.map(currency1)).thenReturn(response1);
        when(currencyMapper.map(currency2)).thenReturn(response2);

        // when
        Page<CurrencyResponse> result = currencyService.getCurrenciesWithPagination(page, size);

        // then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(response1, result.getContent().get(0));
        assertEquals(response2, result.getContent().get(1));

        verify(currencyRepository).findAll(PageRequest.of(page, size));
        verify(currencyMapper).map(currency1);
        verify(currencyMapper).map(currency2);
    }

    @Test
    void addCurrency_shouldAddCurrencySuccessfully() {
        // given
        String currencyName = "USD";
        Currency newCurrency = new Currency(null, "USD");
        Currency savedCurrency = new Currency(1L, "USD");
        CurrencyResponse response = new CurrencyResponse( "USD");

        when(currencyRepository.existsByName(currencyName)).thenReturn(false);
        when(currencyMapper.mapToEntity(currencyName)).thenReturn(newCurrency);
        when(currencyRepository.save(newCurrency)).thenReturn(savedCurrency);
        when(currencyMapper.map(savedCurrency)).thenReturn(response);

        // when
        CurrencyResponse result = currencyService.addCurrency(currencyName);

        // then
        assertNotNull(result);
        assertEquals(response, result);

        verify(currencyRepository).existsByName(currencyName);
        verify(currencyMapper).mapToEntity(currencyName);
        verify(currencyRepository).save(newCurrency);
        verify(exchangeRateService).fetchCurrencyExchangeRates(savedCurrency);
        verify(currencyMapper).map(savedCurrency);
    }

    @Test
    void addCurrency_shouldThrowExceptionWhenCurrencyExists() {
        // given
        String currencyName = "USD";

        when(currencyRepository.existsByName(currencyName)).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> currencyService.addCurrency(currencyName)
        );

        assertEquals("Currency with this name already exists.", exception.getMessage());

        verify(currencyRepository).existsByName(currencyName);
        verifyNoInteractions(currencyMapper, exchangeRateService);
    }
}
