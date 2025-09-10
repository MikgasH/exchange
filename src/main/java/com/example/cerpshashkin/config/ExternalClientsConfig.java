package com.example.cerpshashkin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for external API clients.
 */
@Configuration
public class ExternalClientsConfig {

    /**
     * RestTemplate bean for making HTTP requests to external APIs.
     *
     * @return configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
