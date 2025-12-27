package com.fazquepaga.taskandpay.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AsaasWebhookEvent {
    private String event;
    private PaymentInfo payment;

    @Data
    @NoArgsConstructor
    public static class PaymentInfo {
        private String id;
        private String customer;
        private String subscription;
        private String externalReference;
        private String status;
        private String checkoutSession;
    }
}
