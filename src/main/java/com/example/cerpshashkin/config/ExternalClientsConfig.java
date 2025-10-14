package com.example.cerpshashkin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ExternalClientsConfig {

    @Bean("fixerRestClient")
    public RestClient fixerRestClient(
            @Value("${api.fixer.url}") final String fixerUrl) {
        return RestClient.builder()
                .baseUrl(fixerUrl)
                .build();
    }

    @Bean("exchangeratesRestClient")
    public RestClient exchangeratesRestClient(
            @Value("${api.exchangerates.url}") final String exchangeratesUrl) {
        return RestClient.builder()
                .baseUrl(exchangeratesUrl)
                .build();
    }

    @Bean("currencyapiRestClient")
    public RestClient currencyapiRestClient(
            @Value("${api.currencyapi.url}") final String currencyapiUrl) {
        return RestClient.builder()
                .baseUrl(currencyapiUrl)
                .build();
    }
}
