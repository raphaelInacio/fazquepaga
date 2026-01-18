package com.fazquepaga.taskandpay.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@org.springframework.test.annotation.DirtiesContext
@org.springframework.test.context.TestPropertySource(properties = {
        "recaptcha.enabled=true",
        "recaptcha.secret-key=test-secret",
        "recaptcha.verify-url=http://localhost:9999/verify", // Dummy URL
        "recaptcha.threshold=0.5"
})
class RecaptchaIntegrationTest {

    @Autowired
    private RecaptchaService recaptchaService;

    @MockitoBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        // Setup default behavior if needed
    }

    @Test
    void shouldReturnTrue_whenScoreIsHigh() {
        RecaptchaResponse response = new RecaptchaResponse();
        response.setSuccess(true);
        response.setScore(0.9f);
        response.setAction("login");

        when(restTemplate.postForObject(any(String.class), any(), eq(RecaptchaResponse.class)))
                .thenReturn(response);

        assertTrue(recaptchaService.verify("valid-token", "login"));
    }

    @Test
    void shouldReturnFalse_whenScoreIsLow() {
        RecaptchaResponse response = new RecaptchaResponse();
        response.setSuccess(true);
        response.setScore(0.1f); // Below default 0.5
        response.setAction("login");

        when(restTemplate.postForObject(any(String.class), any(), eq(RecaptchaResponse.class)))
                .thenReturn(response);

        assertFalse(recaptchaService.verify("valid-token", "login"));
    }

    @Test
    void shouldReturnFalse_whenActionMismatch() {
        RecaptchaResponse response = new RecaptchaResponse();
        response.setSuccess(true);
        response.setScore(0.9f);
        response.setAction("other_action");

        when(restTemplate.postForObject(any(String.class), any(), eq(RecaptchaResponse.class)))
                .thenReturn(response);

        assertFalse(recaptchaService.verify("valid-token", "login"));
    }

    @Test
    void shouldReturnTrue_whenApiFails() {
        RecaptchaResponse response = new RecaptchaResponse();
        response.setSuccess(false);

        // Force exception
        when(restTemplate.postForObject(any(String.class), any(), eq(RecaptchaResponse.class)))
                .thenThrow(new org.springframework.web.client.RestClientException("API Error"));

        // Fail open means returns true
        // assertTrue(recaptchaService.verify("invalid-token", "login"));
        // Wait, verify() catches RestClientException and returns true.
        // My test calls verify().
        assertTrue(recaptchaService.verify("invalid-token", "login"));
    }
}
