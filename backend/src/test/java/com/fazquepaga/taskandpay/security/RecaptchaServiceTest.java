package com.fazquepaga.taskandpay.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecaptchaServiceTest {

    private static final String TEST_SECRET_KEY = "test-secret-key";
    private static final String TEST_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private static final float TEST_THRESHOLD = 0.5f;

    @Mock
    private RestTemplate restTemplate;

    private RecaptchaConfig config;
    private RecaptchaServiceImpl recaptchaService;

    @BeforeEach
    void setUp() {
        config = new RecaptchaConfig();
        config.setEnabled(true);
        config.setSecretKey(TEST_SECRET_KEY);
        config.setVerifyUrl(TEST_VERIFY_URL);
        config.setThreshold(TEST_THRESHOLD);

        recaptchaService = new RecaptchaServiceImpl(config, restTemplate);
    }

    @Test
    void shouldReturnTrue_whenScoreAboveThreshold() {
        // Arrange
        String token = "valid-token";
        RecaptchaResponse response = createResponse(true, 0.9f);
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(RecaptchaResponse.class)))
                .thenReturn(response);

        // Act
        boolean result = recaptchaService.verify(token, "login");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_whenScoreBelowThreshold() {
        // Arrange
        String token = "suspicious-token";
        RecaptchaResponse response = createResponse(true, 0.3f);
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(RecaptchaResponse.class)))
                .thenReturn(response);

        // Act
        boolean result = recaptchaService.verify(token, "register");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalse_whenSuccessIsFalse() {
        // Arrange
        String token = "invalid-token";
        RecaptchaResponse response = createResponse(false, 0.0f);
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(RecaptchaResponse.class)))
                .thenReturn(response);

        // Act
        boolean result = recaptchaService.verify(token, "login");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrue_whenDisabled() {
        // Arrange
        config.setEnabled(false);

        // Act
        boolean result = recaptchaService.verify(null, "login");

        // Assert
        assertThat(result).isTrue();
        verify(restTemplate, never()).postForObject(any(String.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void shouldReturnFalse_whenTokenIsNull() {
        // Arrange - config is enabled but token is null

        // Act
        boolean result = recaptchaService.verify(null, "login");

        // Assert
        assertThat(result).isFalse();
        verify(restTemplate, never()).postForObject(any(String.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void shouldReturnFalse_whenTokenIsBlank() {
        // Act
        boolean result = recaptchaService.verify("   ", "login");

        // Assert
        assertThat(result).isFalse();
        verify(restTemplate, never()).postForObject(any(String.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void shouldReturnTrue_whenApiErrorOccurs_failOpen() {
        // Arrange - API error should fail open (allow) to not block legitimate users
        String token = "valid-token";
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(RecaptchaResponse.class)))
                .thenThrow(new RestClientException("API timeout"));

        // Act
        boolean result = recaptchaService.verify(token, "login");

        // Assert
        assertThat(result).isTrue(); // Fail open
    }

    @Test
    void shouldReturnScore_whenTokenIsValid() {
        // Arrange
        String token = "valid-token";
        RecaptchaResponse response = createResponse(true, 0.85f);
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(RecaptchaResponse.class)))
                .thenReturn(response);

        // Act
        float score = recaptchaService.getScore(token);

        // Assert
        assertThat(score).isEqualTo(0.85f);
    }

    @Test
    void shouldReturnZeroScore_whenTokenIsNull() {
        // Act
        float score = recaptchaService.getScore(null);

        // Assert
        assertThat(score).isZero();
        verify(restTemplate, never()).postForObject(any(String.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void shouldReturnMaxScore_whenDisabled() {
        // Arrange
        config.setEnabled(false);

        // Act
        float score = recaptchaService.getScore(null);

        // Assert
        assertThat(score).isEqualTo(1.0f);
        verify(restTemplate, never()).postForObject(any(String.class), any(HttpEntity.class), any(Class.class));
    }

    private RecaptchaResponse createResponse(boolean success, float score) {
        RecaptchaResponse response = new RecaptchaResponse();
        response.setSuccess(success);
        response.setScore(score);
        response.setHostname("localhost");
        response.setAction("login");
        return response;
    }

}
