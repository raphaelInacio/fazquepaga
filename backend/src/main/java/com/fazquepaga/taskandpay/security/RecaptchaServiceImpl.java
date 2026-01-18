package com.fazquepaga.taskandpay.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fazquepaga.taskandpay.security.RecaptchaResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation of RecaptchaService that verifies tokens against Google's
 * reCAPTCHA v3 API.
 */
@Service
public class RecaptchaServiceImpl implements RecaptchaService {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaServiceImpl.class);

    private final RecaptchaConfig config;
    private final RestTemplate restTemplate;

    public RecaptchaServiceImpl(RecaptchaConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean verify(String token, String action) {
        if (!config.isEnabled()) {
            log.debug("reCAPTCHA disabled, bypassing verification for action: {}", action);
            return true;
        }

        if (token == null || token.isBlank()) {
            log.warn("reCAPTCHA verification failed: empty token for action: {}", action);
            return false;
        }

        try {
            RecaptchaResponse response = verifyToken(token);

            if (response == null) {
                log.warn("reCAPTCHA verification failed: null response for action: {}", action);
                return false;
            }

            log.info(
                    "reCAPTCHA verification: action={}, success={}, score={}, hostname={}",
                    action,
                    response.isSuccess(),
                    response.getScore(),
                    response.getHostname());

            if (!response.isSuccess()) {
                log.warn(
                        "reCAPTCHA verification failed: success=false, errors={}, action={}",
                        response.getErrorCodes(),
                        action);
                return false;
            }

            if (!action.equals(response.getAction())) {
                log.warn(
                        "reCAPTCHA action mismatch: expected={}, actual={}",
                        action,
                        response.getAction());
                return false;
            }

            boolean scoreAboveThreshold = response.getScore() >= config.getThreshold();
            if (!scoreAboveThreshold) {
                log.warn(
                        "reCAPTCHA score below threshold: score={}, threshold={}, action={}",
                        response.getScore(),
                        config.getThreshold(),
                        action);
            }

            return scoreAboveThreshold;

        } catch (RestClientException e) {
            log.error("reCAPTCHA API error for action {}: {}", action, e.getMessage(), e);
            // Fail open in case of API errors to avoid blocking legitimate users
            // This is a trade-off; for higher security, return false instead
            return true;
        }
    }

    @Override
    public float getScore(String token) {
        if (!config.isEnabled()) {
            return 1.0f; // Return max score when disabled
        }

        if (token == null || token.isBlank()) {
            return 0.0f;
        }

        try {
            RecaptchaResponse response = verifyToken(token);
            return response != null ? response.getScore() : 0.0f;
        } catch (RestClientException e) {
            log.error("reCAPTCHA API error getting score: {}", e.getMessage(), e);
            return 0.0f;
        }
    }

    private RecaptchaResponse verifyToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", config.getSecretKey());
        params.add("response", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        return restTemplate.postForObject(config.getVerifyUrl(), request, RecaptchaResponse.class);
    }

}
