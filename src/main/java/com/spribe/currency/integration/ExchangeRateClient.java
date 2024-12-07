package com.spribe.currency.integration;

import com.spribe.currency.dto.ExchangeRateIntegrationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "exchangeRateClient", url = "${integration.exchange-rate.url}")
public interface ExchangeRateClient {

    @GetMapping("/live")
    ExchangeRateIntegrationResponse getExchangeRates(@RequestParam("access_key") String accessKey,
                                                     @RequestParam("currencies") String currencies,
                                                     @RequestParam("source") String source,
                                                     @RequestParam("format") int format);
}
