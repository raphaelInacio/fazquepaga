package com.fazquepaga.taskandpay.security;

/**
 * Service interface for reCAPTCHA v3 verification.
 * Validates tokens received from the frontend against Google's reCAPTCHA API.
 */
public interface RecaptchaService {

    /**
 * Verifies that a reCAPTCHA v3 token corresponds to the given action and meets the service's score threshold.
 *
 * @param token  the reCAPTCHA token received from the client
 * @param action the expected action name associated with the token (e.g., "login", "register")
 * @return true if the token is valid and its score meets the required threshold, false otherwise
 */
    boolean verify(String token, String action);

    /**
 * Retrieves the reCAPTCHA v3 score associated with the provided token.
 *
 * @param token the reCAPTCHA token issued by the frontend
 * @return the score in the range 0.0 to 1.0; 1.0 indicates a high likelihood of a legitimate human interaction
 */
    float getScore(String token);
}