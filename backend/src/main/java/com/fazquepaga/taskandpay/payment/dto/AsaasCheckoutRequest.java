package com.fazquepaga.taskandpay.payment.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AsaasCheckoutRequest {
    private List<String> chargeTypes;
    private List<String> billingTypes;
    private List<Item> items;
    private SubscriptionInfo subscription;
    private CallbackInfo callback;
    private String externalReference;
    private boolean notificationEnabled;

    @Data
    @Builder
    public static class Item {
        private String name;
        private BigDecimal value;
        private Integer quantity;
    }

    @Data
    @Builder
    public static class SubscriptionInfo {
        private String cycle; // MONTHLY
        private String description;
        private String nextDueDate;
    }

    @Data
    @Builder
    public static class CallbackInfo {
        private String successUrl;
        private String cancelUrl;
    }
}
