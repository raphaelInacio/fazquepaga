package com.fazquepaga.taskandpay.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AsaasConfig {

    @Value("${asaas.api-key}")
    private String apiKey;

    @Value("${asaas.url:https://sandbox.asaas.com/api/v3}")
    private String baseUrl;

    @Bean("asaasRestTemplate")
    public RestTemplate asaasRestTemplate(RestTemplateBuilder builder) {
        return builder
                .rootUri(baseUrl)
                .defaultHeader("access_token", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("User-Agent", "TaskAndPay-System")
                .build();
    }
}
