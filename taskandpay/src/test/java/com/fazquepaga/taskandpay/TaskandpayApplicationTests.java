package com.fazquepaga.taskandpay;

import com.google.auth.Credentials;
import com.google.cloud.NoCredentials;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TaskandpayApplicationTests {

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public GcpProjectIdProvider gcpProjectIdProvider() {

            return () -> "test-project";
        }

        @Bean
        @Primary
        public Credentials googleCredentials() {

            return NoCredentials.getInstance();
        }
    }

    @Test
    void contextLoads() {}
}
