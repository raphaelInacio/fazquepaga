package com.fazquepaga.taskandpay.identity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @DocumentId private String id;

    private String name;
    private String email; // Used for parents
    private Role role;
    private String parentId; // Links a child to a parent
    private String phoneNumber; // Used for children (WhatsApp) AND Parents (Unique login/contact)
    private String password; // Hashed password for parents
    private String accessCode; // Unique 6-character code for children
    private java.math.BigDecimal monthlyAllowance;
    private java.math.BigDecimal balance; // Current balance for the child
    private Integer age; // Used for children
    private String aiContext; // Free text context about the child for AI prompts
    private SubscriptionTier subscriptionTier; // Subscription tier (only for PARENT role)
    private SubscriptionStatus subscriptionStatus; // Subscription status (only for PARENT role)
    private String asaasCustomerId; // Asaas Customer ID (only for PARENT role)
    private String subscriptionId; // Asaas Subscription ID (only for PARENT role)
    private String document; // CPF/CNPJ for payment registration

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

    // UserDetails Implementation

    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>
            getAuthorities() {
        return java.util.Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        // For parents, use email. For children, we might use ID or Access Code,
        // but this method is primarily for Spring Security context.
        return email != null ? email : id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
