package com.fazquepaga.taskandpay.subscription;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.payment.AsaasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final UserRepository userRepository;
    private final AsaasService asaasService;

    public String generateSubscribeUrl(String userId) {
        User user = getUser(userId);

        if (user.getSubscriptionTier() == User.SubscriptionTier.PREMIUM
                && user.getSubscriptionStatus() == User.SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("User is already PREMIUM");
        }

        // Ensure Asaas Customer exists
        // Ensure Asaas Customer exists
        // Refactored: We rely on Checkout Session to create the customer and link via
        // session ID
        // if (user.getAsaasCustomerId() == null) {
        // String customerId = asaasService.createCustomer(user);
        // user.setAsaasCustomerId(customerId);
        // }

        return asaasService.createCheckoutSession(user);
    }

    public User.SubscriptionStatus getStatus(String userId) {
        return getUser(userId).getSubscriptionStatus();
    }

    public User.SubscriptionTier getTier(String userId) {
        return getUser(userId).getSubscriptionTier();
    }

    public User getUser(String userId) {
        try {
            return userRepository.findByIdSync(userId);
        } catch (Exception e) {
            throw new RuntimeException("User not found", e);
        }
    }

    public void activateSubscription(String asaasCustomerId, String subscriptionId, String checkoutSessionId) {
        try {
            User user = null;
            if (asaasCustomerId != null) {
                user = userRepository.findByAsaasCustomerId(asaasCustomerId);
            }

            // Fallback: lookup by checkoutSessionId
            if (user == null && checkoutSessionId != null) {
                user = userRepository.findByLastCheckoutSessionId(checkoutSessionId);
                // Sync: If found by session, update the customerId to match the new one from
                // webhook
                if (user != null && asaasCustomerId != null
                        && !asaasCustomerId.equals(user.getAsaasCustomerId())) {
                    log.info("Updating User {} AsaasCustomerId from {} to {}", user.getId(),
                            user.getAsaasCustomerId(), asaasCustomerId);
                    user.setAsaasCustomerId(asaasCustomerId);
                }
            }

            if (user != null) {
                user.setSubscriptionStatus(User.SubscriptionStatus.ACTIVE);
                user.setSubscriptionTier(User.SubscriptionTier.PREMIUM);
                user.setSubscriptionId(subscriptionId);
                userRepository.save(user);
                log.info("Subscription activated for user: {}. CustomerID: {}", user.getId(), asaasCustomerId);
            } else {
                log.error("User not found for subscription activation. CustomerID: {}, Session: {}",
                        asaasCustomerId, checkoutSessionId);
            }
        } catch (Exception e) {
            log.error("Error activating subscription", e);
            throw new RuntimeException(e);
        }
    }

    public void deactivateSubscription(String asaasCustomerId, User.SubscriptionStatus status,
            String checkoutSessionId) {
        try {
            User user = null;
            if (asaasCustomerId != null) {
                user = userRepository.findByAsaasCustomerId(asaasCustomerId);
            }

            // Fallback: lookup by checkoutSessionId
            if (user == null && checkoutSessionId != null) {
                user = userRepository.findByLastCheckoutSessionId(checkoutSessionId);
                // Sync logic mirrors activation, though less critical for deactivation it keeps
                // data
                // consistent
                if (user != null && asaasCustomerId != null
                        && !asaasCustomerId.equals(user.getAsaasCustomerId())) {
                    log.info("Updating User {} AsaasCustomerId from {} to {}", user.getId(),
                            user.getAsaasCustomerId(), asaasCustomerId);
                    user.setAsaasCustomerId(asaasCustomerId);
                }
            }

            if (user != null) {
                user.setSubscriptionStatus(status);
                user.setSubscriptionTier(User.SubscriptionTier.FREE);
                userRepository.save(user);
                log.info("Subscription deactivated for user: {}. Status: {}", user.getId(), status);
            } else {
                log.error("User not found for subscription deactivation. CustomerID: {}, Session: {}",
                        asaasCustomerId, checkoutSessionId);
            }
        } catch (Exception e) {
            log.error("Error deactivating subscription", e);
            throw new RuntimeException(e);
        }
    }

    // Permission Checks

    public boolean canCreateTask(User user, int currentTasks) {
        if (isPremium(user))
            return true;
        return currentTasks < 50; // Free limit example
    }

    public boolean canAccessGiftCardStore(User user) {
        return isPremium(user);
    }

    public boolean canUseAI(User user) {
        return isPremium(user);
    }

    public boolean canAddChild(User user, int currentChildren) {
        if (isPremium(user))
            return true;
        return currentChildren < 2; // Free limit
    }

    public int getMaxRecurringTasks(User user) {
        if (isPremium(user))
            return 100;
        return 3;
    }

    private boolean isPremium(User user) {
        return user != null
                && user.getSubscriptionTier() == User.SubscriptionTier.PREMIUM
                && user.getSubscriptionStatus() == User.SubscriptionStatus.ACTIVE;
    }

    // Trial Methods

    /**
     * Checks if the user's trial has expired.
     * Premium users are never considered expired.
     * Users without trialStartDate are treated as expired.
     *
     * @param user the user to check
     * @return true if trial expired, false otherwise
     */
    public boolean isTrialExpired(User user) {
        if (isPremium(user)) {
            return false;
        }
        if (user == null || user.getTrialStartDate() == null) {
            return true;
        }

        java.time.Instant trialEnd = user.getTrialStartDate().plus(3, java.time.temporal.ChronoUnit.DAYS);
        return java.time.Instant.now().isAfter(trialEnd);
    }

    /**
     * Returns the number of days remaining in the trial.
     *
     * @param user the user to check
     * @return null for Premium users, 0 if expired, or days remaining (1-3)
     */
    public Integer getTrialDaysRemaining(User user) {
        if (isPremium(user)) {
            return null;
        }
        if (user == null || user.getTrialStartDate() == null) {
            return 0;
        }

        java.time.Instant trialEnd = user.getTrialStartDate().plus(3, java.time.temporal.ChronoUnit.DAYS);
        long hours = java.time.temporal.ChronoUnit.HOURS.between(java.time.Instant.now(), trialEnd);
        if (hours <= 0) {
            return 0;
        }
        return (int) Math.ceil(hours / 24.0);
    }
}
