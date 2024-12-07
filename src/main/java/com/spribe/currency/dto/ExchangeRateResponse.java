package com.spribe.currency.dto;

import java.math.BigDecimal;

public record ExchangeRateResponse(String baseCurrencyName,
                                   String targetCurrencyName,
                                   BigDecimal rate) {
}
