package com.spribe.currency.controller;

import com.spribe.currency.dto.ExchangeRateResponse;
import com.spribe.currency.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExchangeRateService exchangeRateService;

    @Test
    void testGetExchangeRates() throws Exception {
        // given
        String currency = "USD";
        List<ExchangeRateResponse> mockRates = List.of(
                new ExchangeRateResponse("USD", "EUR", BigDecimal.valueOf(0.85)),
                new ExchangeRateResponse("USD", "GBP", BigDecimal.valueOf(0.75))
        );

        Mockito.when(exchangeRateService.getExchangeRates(currency)).thenReturn(mockRates);

        // when - then
        mockMvc.perform(get("/api/exchange-rates")
                                .param("currency", currency)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].baseCurrencyName").value("USD"))
                .andExpect(jsonPath("$[0].targetCurrencyName").value("EUR"))
                .andExpect(jsonPath("$[0].rate").value(0.85))
                .andExpect(jsonPath("$[1].baseCurrencyName").value("USD"))
                .andExpect(jsonPath("$[1].targetCurrencyName").value("GBP"))
                .andExpect(jsonPath("$[1].rate").value(0.75));
    }
}
