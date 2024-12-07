package com.spribe.currency.controller;

import com.spribe.currency.dto.CurrencyResponse;
import com.spribe.currency.service.CurrencyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrencyService currencyService;

    @Test
    void testGetCurrencies() throws Exception {
        // given
        List<CurrencyResponse> currencies = List.of(
                new CurrencyResponse( "USD"),
                new CurrencyResponse( "EUR")
        );
        Page<CurrencyResponse> page = new PageImpl<>(currencies, PageRequest.of(0, 10), 2);

        Mockito.when(currencyService.getCurrenciesWithPagination(anyInt(), anyInt())).thenReturn(page);

        // when - then
        mockMvc.perform(get("/api/currencies")
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("USD"))
                .andExpect(jsonPath("$.content[1].name").value("EUR"));
    }

    @Test
    void testAddCurrency() throws Exception {
        // given
        CurrencyResponse response = new CurrencyResponse( "USD");
        Mockito.when(currencyService.addCurrency("USD")).thenReturn(response);

        // when - then
        mockMvc.perform(post("/api/currencies")
                                .param("name", "USD")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("USD"));
    }
}
