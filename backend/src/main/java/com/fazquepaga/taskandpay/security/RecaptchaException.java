package com.fazquepaga.taskandpay.security;

/**
 * Exception thrown when reCAPTCHA verification fails.
 */
public class RecaptchaException extends RuntimeException {

    /**
     * Creates a new RecaptchaException with the specified detail message.
     *
     * @param message the detail message describing the reCAPTCHA verification failure
     */
    public RecaptchaException(String message) {
        super(message);
    }

    /**
     * Constructs a new RecaptchaException with the specified detail message and cause.
     *
     * @param message the detail message explaining the reCAPTCHA verification failure
     * @param cause the underlying cause of this exception, or {@code null} if none
     */
    public RecaptchaException(String message, Throwable cause) {
        super(message, cause);
    }
}