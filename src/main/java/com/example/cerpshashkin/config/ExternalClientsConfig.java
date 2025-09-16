package com.example.cerpshashkin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ExternalClientsConfig {

    @Value("${api.fixer.url}")
    private String fixerUrl;

    @Value("${api.exchangerates.url}")
    private String exchangeratesUrl;

    @Value("${api.currencyapi.url}")
    private String currencyapiUrl;

    @Bean("fixerRestClient")
    public RestClient fixerRestClient() {
        return RestClient.builder()
                .baseUrl(fixerUrl)
                .build();
    }

    @Bean("exchangeratesRestClient")
    public RestClient exchangeratesRestClient() {
        return RestClient.builder()
                .baseUrl(exchangeratesUrl)
                .build();
    }

    @Bean("currencyapiRestClient")
    public RestClient currencyapiRestClient() {
        return RestClient.builder()
                .baseUrl(currencyapiUrl)
                .build();
    }
}
