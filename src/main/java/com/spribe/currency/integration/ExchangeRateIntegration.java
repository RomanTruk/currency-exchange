package com.spribe.currency.integration;

import com.spribe.currency.dto.ExchangeRateIntegrationResponse;
import com.spribe.currency.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Set;

@RequiredArgsConstructor
@Component
public class ExchangeRateIntegration {

    private final ExchangeRateClient exchangeRateClient;

    @Value("${integration.exchange-rate.key}")
    private String accessKey;

    @Value("${integration.exchange-rate.format}")
    private int format;

    @Retryable(retryFor = {RuntimeException.class}, backoff = @Backoff(delay = 2000))
    public ExchangeRateIntegrationResponse getExchangeRates(Set<String> currencies, String source) {
        try {
            ExchangeRateIntegrationResponse response = exchangeRateClient.getExchangeRates(
                    accessKey, StringUtils.joinList(currencies), source, format);

            if (response != null && response.success()) {
                return response;
            } else {
                throw new RuntimeException("Failed to fetch exchange rates from API");
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching exchange rates", e);
        }
    }
}
