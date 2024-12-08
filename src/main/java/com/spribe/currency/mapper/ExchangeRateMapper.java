package com.spribe.currency.mapper;

import com.spribe.currency.dto.ExchangeRateResponse;
import com.spribe.currency.entity.Currency;
import com.spribe.currency.entity.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ExchangeRateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updated", source = "timestamp")
    ExchangeRate mapToEntity(Currency baseCurrency, Currency targetCurrency, BigDecimal rate, long timestamp);

    @Mapping(target = "baseCurrencyName", source = "baseCurrency.name")
    @Mapping(target = "targetCurrencyName", source = "targetCurrency.name")
    ExchangeRateResponse toResponse(ExchangeRate exchangeRate);

    List<ExchangeRateResponse> toResponses(List<ExchangeRate> exchangeRates);
}
