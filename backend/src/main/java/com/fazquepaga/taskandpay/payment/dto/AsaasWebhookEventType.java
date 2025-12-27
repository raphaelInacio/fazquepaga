package com.fazquepaga.taskandpay.payment.dto;

public enum AsaasWebhookEventType {
    PAYMENT_CONFIRMED,
    PAYMENT_RECEIVED,
    PAYMENT_OVERDUE,
    PAYMENT_REFUNDED,
    CHARGEBACK_REQUESTED,
    SUBSCRIPTION_CREATED,
    UNKNOWN;

    public static AsaasWebhookEventType fromString(String event) {
        try {
            return valueOf(event);
        } catch (IllegalArgumentException | NullPointerException e) {
            return UNKNOWN;
        }
    }
}
