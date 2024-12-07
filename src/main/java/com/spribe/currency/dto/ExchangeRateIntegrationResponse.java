package com.spribe.currency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRateIntegrationResponse(
        boolean success,
        String terms,
        String privacy,
        long timestamp,
        String source,
        @JsonProperty("quotes")
        Map<String, BigDecimal> rates
) {
}
