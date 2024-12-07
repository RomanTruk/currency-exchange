package com.spribe.currency.controller;

import com.spribe.currency.dto.CurrencyResponse;
import com.spribe.currency.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<Page<CurrencyResponse>> getCurrencies(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Page<CurrencyResponse> currencyPage = currencyService.getCurrenciesWithPagination(page, size);
        return ResponseEntity.ok(currencyPage);
    }

    @PostMapping
    public ResponseEntity<CurrencyResponse> addCurrency(
            @RequestParam(name = "name") String name
    ) {
        return ResponseEntity.ok(currencyService.addCurrency(name));
    }
}
