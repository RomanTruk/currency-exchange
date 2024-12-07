package com.spribe.currency.mapper;

import com.spribe.currency.dto.ExchangeRateResponse;
import com.spribe.currency.entity.Currency;
import com.spribe.currency.entity.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ExchangeRateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updated", source = "timestamp", qualifiedByName = "longToLocalDateTime")
    ExchangeRate mapToEntity(Currency baseCurrency, Currency targetCurrency, BigDecimal rate, long timestamp);

    @Named("longToLocalDateTime")
    default LocalDateTime longToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);
    }

    @Mapping(target = "baseCurrencyName", source = "baseCurrency.name")
    @Mapping(target = "targetCurrencyName", source = "targetCurrency.name")
    ExchangeRateResponse toResponse(ExchangeRate exchangeRate);

    List<ExchangeRateResponse> toResponses(List<ExchangeRate> exchangeRates);
}
