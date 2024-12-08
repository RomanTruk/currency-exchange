package com.spribe.currency.mapper;

import com.spribe.currency.dto.CurrencyResponse;
import com.spribe.currency.entity.Currency;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CurrencyMapperTest {

    private final CurrencyMapper currencyMapper = Mappers.getMapper(CurrencyMapper.class);

    @Test
    void testMapCurrencyToCurrencyResponse() {
        // given
        Currency currency = new Currency();
        currency.setId(1L);
        currency.setName("USD");

        // when
        CurrencyResponse response = currencyMapper.map(currency);

        // then
        assertEquals(currency.getName(), response.name());
    }

    @Test
    void testMapListOfCurrenciesToResponses() {
        // given
        Currency currency1 = new Currency();
        currency1.setId(1L);
        currency1.setName("USD");

        Currency currency2 = new Currency();
        currency2.setId(2L);
        currency2.setName("EUR");

        List<Currency> currencies = List.of(currency1, currency2);

        // when
        List<CurrencyResponse> responses = currencyMapper.map(currencies);

        // then
        assertEquals(currencies.size(), responses.size());
        assertEquals("USD", responses.get(0).name());
        assertEquals("EUR", responses.get(1).name());
    }

    @Test
    void testMapToEntity() {
        // given
        String name = "CAD";

        // when
        Currency currency = currencyMapper.mapToEntity(name);

        // then
        assertNull(currency.getId());
        assertEquals(name, currency.getName());
    }
}
