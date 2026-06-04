package com.fazquepaga.taskandpay.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaasWebhookEvent {
    private String event;
    private PaymentInfo payment;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentInfo {
        private String id;
        private String customer;
        private String subscription;
        private String externalReference;
        private String status;
        private String checkoutSession;
    }
}
