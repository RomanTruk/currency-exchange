package com.spribe.currency.integration;

import com.spribe.currency.dto.ExchangeRateIntegrationResponse;
import com.spribe.currency.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExchangeRateIntegration {

    private final ExchangeRateClient exchangeRateClient;

    @Value("${integration.exchange-rate.key}")
    private String accessKey;

    @Value("${integration.exchange-rate.format}")
    private int format;

    @Retryable(retryFor = {RuntimeException.class},
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${retry.maxDelay}")
    )
    public ExchangeRateIntegrationResponse getExchangeRates(Set<String> currencies, String source) {
        ExchangeRateIntegrationResponse response;
        try {
            response = exchangeRateClient.getExchangeRates(
                    accessKey, StringUtils.joinList(currencies), source, format);

            if (response == null || !response.success()) {
                throw new RuntimeException("Failed to fetch exchange rates from API");
            }
        } catch (feign.FeignException e) {
            log.error("Error fetch exchange rates from API integration", e);
            throw new RuntimeException(
                    String.format("Exchange rates API: return code %d", e.status())
            );
        }

        return response;
    }
}
