package com.spribe.currency.service.scheduled;

import com.spribe.currency.entity.Currency;
import com.spribe.currency.repositories.CurrencyRepository;
import com.spribe.currency.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExchangeRateScheduler {

    private final CurrencyRepository currencyRepository;
    private final ExchangeRateService exchangeRateService;

    @Scheduled(cron = "${scheduling.cron.fetch-exchange-rates}")
    public void fetchAndUpdateExchangeRates() {
        log.info("Starting scheduled exchange rate update.");
        try {
            List<Currency> currencies = currencyRepository.findAll();
            if (currencies.isEmpty()) {
                log.info("No currencies found in the database.");
                return;
            }

            List<CompletableFuture<Void>> futures = currencies.stream()
                    .map(currency -> CompletableFuture.runAsync(() -> {
                        try {
                            exchangeRateService.fetchCurrencyExchangeRatesV2(currency);
                        } catch (Exception e) {
                            log.error("Failed to fetch exchange rates for currency: {}", currency.getName(), e);
                        }
                    }))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            log.info("Completed fetching exchange rates for all currencies.");
        } catch (Exception e) {
            log.error("Error occurred during scheduled exchange rate update", e);
        }

        log.info("Scheduled exchange rate update completed.");
    }
}
