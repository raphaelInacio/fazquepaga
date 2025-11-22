package com.fazquepaga.taskandpay.subscription;

/**
 * Exception thrown when a user attempts to perform an action that exceeds their
 * subscription tier limits.
 */
public class SubscriptionLimitReachedException extends RuntimeException {

    public SubscriptionLimitReachedException(String message) {
        super(message);
    }

    public SubscriptionLimitReachedException(String message, Throwable cause) {
        super(message, cause);
    }
}
