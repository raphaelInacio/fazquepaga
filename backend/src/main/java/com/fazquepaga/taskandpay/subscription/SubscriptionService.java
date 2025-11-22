package com.fazquepaga.taskandpay.subscription;

import com.fazquepaga.taskandpay.identity.User;
import org.springframework.stereotype.Service;

/**
 * Service responsible for managing subscription-related business logic.
 * Handles permission checks based on subscription tiers.
 */
@Service
public class SubscriptionService {

    // Configuration constants for Free tier limits
    private static final int FREE_TIER_MAX_RECURRING_TASKS = 5;
    private static final int FREE_TIER_MAX_CHILDREN = 1;

    /**
     * Checks if a user can create a new task based on their subscription tier.
     *
     * @param user                      The user attempting to create a task
     * @param currentRecurringTaskCount The current number of recurring tasks
     * @return true if the user can create a task, false otherwise
     */
    public boolean canCreateTask(User user, int currentRecurringTaskCount) {
        if (user == null || user.getSubscriptionTier() == null) {
            return false;
        }

        // Premium users have unlimited tasks
        if (user.getSubscriptionTier() == User.SubscriptionTier.PREMIUM) {
            return true;
        }

        // Free users are limited to FREE_TIER_MAX_RECURRING_TASKS
        return currentRecurringTaskCount < FREE_TIER_MAX_RECURRING_TASKS;
    }

    /**
     * Checks if a user can use AI features (suggestions, validation).
     *
     * @param user The user attempting to use AI features
     * @return true if the user can use AI, false otherwise
     */
    public boolean canUseAI(User user) {
        if (user == null || user.getSubscriptionTier() == null) {
            return false;
        }

        // Only Premium users can use AI features
        return user.getSubscriptionTier() == User.SubscriptionTier.PREMIUM;
    }

    /**
     * Checks if a user can add a new child.
     *
     * @param user              The user attempting to add a child
     * @param currentChildCount The current number of children
     * @return true if the user can add a child, false otherwise
     */
    public boolean canAddChild(User user, int currentChildCount) {
        if (user == null || user.getSubscriptionTier() == null) {
            return false;
        }

        // Premium users can have unlimited children (future feature)
        if (user.getSubscriptionTier() == User.SubscriptionTier.PREMIUM) {
            return true;
        }

        // Free users are limited to FREE_TIER_MAX_CHILDREN
        return currentChildCount < FREE_TIER_MAX_CHILDREN;
    }

    /**
     * Checks if a user can access the Gift Card store.
     *
     * @param user The user attempting to access the store
     * @return true if the user can access the store, false otherwise
     */
    public boolean canAccessGiftCardStore(User user) {
        if (user == null || user.getSubscriptionTier() == null) {
            return false;
        }

        // Only Premium users can access the Gift Card store
        return user.getSubscriptionTier() == User.SubscriptionTier.PREMIUM;
    }

    /**
     * Gets the maximum number of recurring tasks allowed for a user.
     *
     * @param user The user
     * @return The maximum number of recurring tasks, or -1 for unlimited
     */
    public int getMaxRecurringTasks(User user) {
        if (user == null || user.getSubscriptionTier() == null) {
            return 0;
        }

        if (user.getSubscriptionTier() == User.SubscriptionTier.PREMIUM) {
            return -1; // Unlimited
        }

        return FREE_TIER_MAX_RECURRING_TASKS;
    }
}
