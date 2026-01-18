package com.fazquepaga.taskandpay.security;

/**
 * Service interface for reCAPTCHA v3 verification.
 * Validates tokens received from the frontend against Google's reCAPTCHA API.
 */
public interface RecaptchaService {

    /**
     * Verifies a reCAPTCHA token for a specific action.
     *
     * @param token  The reCAPTCHA token from the frontend
     * @param action The action name (e.g., "login", "register")
     * @return true if the token is valid and score is above threshold
     */
    boolean verify(String token, String action);

    /**
     * Gets the reCAPTCHA score for a token.
     *
     * @param token The reCAPTCHA token from the frontend
     * @return The score between 0.0 and 1.0 (1.0 is very likely a good interaction)
     */
    float getScore(String token);
}
