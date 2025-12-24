package com.fazquepaga.taskandpay.subscription;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.payment.AsaasService;
import java.util.Optional;
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
        if (user.getAsaasCustomerId() == null) {
            String customerId = asaasService.createCustomer(user);
            user.setAsaasCustomerId(customerId);
            // Save updated user with customer ID if needed immediately,
            // though createCustomer inside AsaasService might have saved it.
            // Let's rely on AsaasService returning the ID and ensure consistency.
        }

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

    public void activateSubscription(String externalReference, String subscriptionId) {
        try {
            Optional<User> userOpt =
                    Optional.ofNullable(userRepository.findByIdSync(externalReference));
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setSubscriptionStatus(User.SubscriptionStatus.ACTIVE);
                user.setSubscriptionTier(User.SubscriptionTier.PREMIUM);
                user.setSubscriptionId(subscriptionId);
                userRepository.save(user);
                log.info("Subscription activated for user: {}", externalReference);
            } else {
                log.error("User not found for subscription activation: {}", externalReference);
            }
        } catch (Exception e) {
            log.error("Error activating subscription", e);
            throw new RuntimeException(e);
        }
    }

    // Permission Checks

    public boolean canCreateTask(User user, int currentTasks) {
        if (isPremium(user)) return true;
        return currentTasks < 50; // Free limit example
    }

    public boolean canAccessGiftCardStore(User user) {
        return isPremium(user);
    }

    public boolean canUseAI(User user) {
        return isPremium(user);
    }

    public boolean canAddChild(User user, int currentChildren) {
        if (isPremium(user)) return true;
        return currentChildren < 2; // Free limit
    }

    public int getMaxRecurringTasks(User user) {
        if (isPremium(user)) return 100;
        return 3;
    }

    private boolean isPremium(User user) {
        return user != null
                && user.getSubscriptionTier() == User.SubscriptionTier.PREMIUM
                && user.getSubscriptionStatus() == User.SubscriptionStatus.ACTIVE;
    }
}
