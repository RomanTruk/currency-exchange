package com.spribe.currency.service;

import com.spribe.currency.dto.ExchangeRateIntegrationResponse;
import com.spribe.currency.entity.Currency;
import com.spribe.currency.entity.ExchangeRate;
import com.spribe.currency.integration.ExchangeRateIntegration;
import com.spribe.currency.mapper.ExchangeRateMapper;
import com.spribe.currency.repositories.CurrencyRepository;
import com.spribe.currency.repositories.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExchangeRateService {

    private final ExchangeRateMapper exchangeRateMapper;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateIntegration exchangeRateIntegration;
    private final Map<String, List<ExchangeRate>> exchangeRatesCache = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public List<ExchangeRate> getExchangeRates(String currency) {
        log.info("Getting exchange rate for currency: {}.", currency);
        return exchangeRatesCache.get(currency);
    }

    @Transactional
    public void fetchCurrencyExchangeRates(Currency currency) {
        log.info("Fetching exchange rate for currency: {}.", currency);

        List<Currency> allCurrencies = currencyRepository.findAll().stream()
                .filter(dbCurrency -> !dbCurrency.getName().equals(currency.getName()))
                .toList();

        if (allCurrencies.isEmpty()) {
            log.info("No currencies found to update exchange rates.");
            return;
        }

        Map<String, Currency> nameCurrencyMap = allCurrencies.stream()
                .collect(Collectors.toMap(Currency::getName, Function.identity()));

        ExchangeRateIntegrationResponse integrationResponse = exchangeRateIntegration
                .getExchangeRates(nameCurrencyMap.keySet(), currency.getName());

        List<ExchangeRate> exchangeRates = getExchangeRates(currency, integrationResponse, nameCurrencyMap);

        exchangeRateRepository.saveAll(exchangeRates);
        mergeExchangeRatesIntoCache(exchangeRates);
    }

    @Transactional
    public void fetchCurrencyExchangeRatesV2(Currency currency) {
        log.info("Fetching exchange rate for currency: {}.", currency);

        List<Currency> allCurrencies = currencyRepository.findAll().stream()
                .filter(dbCurrency -> !dbCurrency.getName().equals(currency.getName()))
                .toList();

        if (allCurrencies.isEmpty()) {
            log.info("No currencies found to update exchange rates.");
            return;
        }

        Map<String, Currency> nameCurrencyMap = allCurrencies.stream()
                .collect(Collectors.toMap(Currency::getName, Function.identity()));

        ExchangeRateIntegrationResponse integrationResponse = exchangeRateIntegration
                .getExchangeRates(nameCurrencyMap.keySet(), currency.getName());

        List<ExchangeRate> fetchedRates = getExchangeRates(currency, integrationResponse, nameCurrencyMap);
        List<ExchangeRate> dbRates = exchangeRateRepository.findByBaseOrTargetCurrency(currency);

        var currencyUpdatedRateMap = fetchedRates.stream()
                .collect(Collectors.toMap(r -> Pair.of(r.getBaseCurrency(), r.getTargetCurrency()), Function.identity()));

        dbRates.forEach(exchangeRate -> {
            Pair<Currency, Currency> pair = Pair.of(exchangeRate.getBaseCurrency(), exchangeRate.getTargetCurrency());
            ExchangeRate rate = Optional.ofNullable(currencyUpdatedRateMap.get(pair)).orElseThrow();
            exchangeRate.setRate(rate.getRate());
            exchangeRate.setUpdated(rate.getUpdated());
        });

        mergeExchangeRatesIntoCache(dbRates);
    }

    private List<ExchangeRate> getExchangeRates(Currency currency,
                                                ExchangeRateIntegrationResponse integrationResponse,
                                                Map<String, Currency> nameCurrencyMap) {
        return integrationResponse.rates().entrySet().stream()
                .map(entry -> {
                    String targetCurrencyName = entry.getKey().substring(integrationResponse.source().length());
                    Currency targetCurrency = Optional.ofNullable(nameCurrencyMap.get(targetCurrencyName))
                            .orElseThrow(() -> new RuntimeException("Target currency not found"));
                    ExchangeRate exchangeRateForward = exchangeRateMapper.mapToEntity(
                            currency,
                            targetCurrency,
                            entry.getValue(),
                            integrationResponse.timestamp()
                    );
                    ExchangeRate exchangeRateReverse = exchangeRateMapper.mapToEntity(
                            targetCurrency,
                            currency,
                            BigDecimal.ONE.divide(entry.getValue(), RoundingMode.HALF_UP),
                            integrationResponse.timestamp()
                    );
                    return List.of(exchangeRateForward, exchangeRateReverse);
                })
                .flatMap(List::stream)
                .toList();
    }

    private void mergeExchangeRatesIntoCache(List<ExchangeRate> rates) {
        rates.stream().collect(Collectors.groupingBy(ExchangeRate::getBaseCurrency))
                .forEach((baseCurrency, newRates) -> exchangeRatesCache.merge(
                                 baseCurrency.getName(),
                                 newRates,
                                 (existingRates, incomingRates) -> {
                                     Map<String, ExchangeRate> uniqueRates =
                                             Stream.concat(existingRates.stream(), incomingRates.stream())
                                                     .collect(Collectors.toMap(
                                                             rate -> rate.getTargetCurrency().getName(),
                                                             Function.identity(),
                                                             (existing, incoming) -> incoming
                                                     ));
                                     return new ArrayList<>(uniqueRates.values());
                                 }
                         )
                );
    }

}
