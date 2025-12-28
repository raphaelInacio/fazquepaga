package com.fazquepaga.taskandpay.subscription.dto;

import com.fazquepaga.taskandpay.identity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionStatusResponse {
    private User.SubscriptionTier tier;
    private User.SubscriptionStatus status;
    private String subscriptionId;
    // Trial fields
    private boolean isTrialActive;
    private Integer trialDaysRemaining;
}
