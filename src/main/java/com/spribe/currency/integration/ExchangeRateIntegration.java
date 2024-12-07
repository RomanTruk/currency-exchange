package com.spribe.currency.integration;

import com.spribe.currency.dto.ExchangeRateIntegrationResponse;
import com.spribe.currency.entity.Currency;
import com.spribe.currency.entity.ExchangeRate;
import com.spribe.currency.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class ExchangeRateIntegration {

    private final ExchangeRateClient exchangeRateClient;

    @Value("${integration.exchange-rate.key}")
    private String accessKey;

    @Value("${integration.exchange-rate.format}")
    private int format;

    public ExchangeRateIntegrationResponse getExchangeRates(Set<String> currencies, String source) {
        try {
//            ExchangeRateIntegrationResponse response = exchangeRateClient.getExchangeRates(
//                    accessKey, StringUtils.joinList(currencies), source, format);

            Map<String, BigDecimal> mockRates = getMockExchangeRates(currencies, source);
            var response = new ExchangeRateIntegrationResponse(
                    true,
                    "terms-and-conditions-url",
                    "privacy-policy-url",
                    System.currentTimeMillis(),
                    source,
                    mockRates
            );

            if (response != null && response.success()) {
                return response;
            } else {
                throw new RuntimeException("Failed to fetch exchange rates from API");
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching exchange rates", e);
        }
    }

    private Map<String, BigDecimal> getMockExchangeRates(Set<String> currencies, String source) {
        Map<String, BigDecimal> exchangeRates = new HashMap<>();

        for (String targetCurrency : currencies) {
            String currencyPair = source + targetCurrency;
            BigDecimal rate = BigDecimal.valueOf(Math.random() * 100);
            exchangeRates.put(currencyPair, rate);
        }

        return exchangeRates;
    }
}
