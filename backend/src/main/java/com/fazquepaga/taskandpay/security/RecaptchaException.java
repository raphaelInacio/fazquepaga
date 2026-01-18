package com.fazquepaga.taskandpay.security;

/**
 * Exception thrown when reCAPTCHA verification fails.
 */
public class RecaptchaException extends RuntimeException {

    public RecaptchaException(String message) {
        super(message);
    }

    public RecaptchaException(String message, Throwable cause) {
        super(message, cause);
    }
}
