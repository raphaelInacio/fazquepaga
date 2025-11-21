package com.fazquepaga.taskandpay.shared;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskAndPayOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("TaskAndPay API")
                                .description("API for TaskAndPay Application")
                                .version("v0.0.1"));
    }
}
