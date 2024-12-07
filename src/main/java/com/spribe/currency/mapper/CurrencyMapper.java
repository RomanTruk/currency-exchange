package com.spribe.currency.mapper;

import com.spribe.currency.dto.CurrencyResponse;
import com.spribe.currency.entity.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CurrencyMapper {

    CurrencyResponse map(Currency currency);

    List<CurrencyResponse> map(List<Currency> currencies);

    @Mapping(target = "id", ignore = true)
    Currency mapToEntity(String name);

}
