package com.spribe.currency.service;

import com.spribe.currency.dto.CurrencyResponse;
import com.spribe.currency.entity.Currency;
import com.spribe.currency.mapper.CurrencyMapper;
import com.spribe.currency.repositories.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CurrencyService {

    private final CurrencyMapper currencyMapper;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateService exchangeRateService;

    @Transactional(readOnly = true)
    public Page<CurrencyResponse> getCurrenciesWithPagination(int page, int size) {
        Page<Currency> currenciesPage = currencyRepository.findAll(PageRequest.of(page, size));

        return currenciesPage.map(currencyMapper::map);
    }

    @Transactional
    public CurrencyResponse addCurrency(String name) {
        if (currencyRepository.existsByName(name)) {
            throw new IllegalArgumentException("Currency with this name already exists.");
        }
        Currency savedCurrency = currencyRepository.save(currencyMapper.mapToEntity(name));
        exchangeRateService.fetchCurrencyExchangeRates(savedCurrency);

        return currencyMapper.map(savedCurrency);
    }
}
