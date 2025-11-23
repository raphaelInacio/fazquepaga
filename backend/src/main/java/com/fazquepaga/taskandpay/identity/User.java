package com.fazquepaga.taskandpay.identity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @DocumentId
    private String id;

    private String name;
    private String email; // Used for parents
    private Role role;
    private String parentId; // Links a child to a parent
    private String phoneNumber; // Used for children for WhatsApp identification
    private java.math.BigDecimal monthlyAllowance;
    private java.math.BigDecimal balance; // Current balance for the child
    private Integer age; // Used for children
    private SubscriptionTier subscriptionTier; // Subscription tier (only for PARENT role)
    private SubscriptionStatus subscriptionStatus; // Subscription status (only for PARENT role)

    public enum Role {
        PARENT,
        CHILD
    }

    public enum SubscriptionTier {
        FREE,
        PREMIUM
    }

    public enum SubscriptionStatus {
        ACTIVE,
        CANCELED,
        PAST_DUE
    }
}
