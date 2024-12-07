package com.spribe.currency.controller;

import com.spribe.currency.dto.ExchangeRateResponse;
import com.spribe.currency.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping
    public ResponseEntity<List<ExchangeRateResponse>> getExchangeRates(@RequestParam("currency") String currency) {
        List<ExchangeRateResponse> rates = exchangeRateService.getExchangeRates(currency);
        return ResponseEntity.ok(rates);
    }
}
