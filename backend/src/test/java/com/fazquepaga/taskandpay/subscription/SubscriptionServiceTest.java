package com.fazquepaga.taskandpay.subscription;

import static org.junit.jupiter.api.Assertions.*;

import com.fazquepaga.taskandpay.identity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubscriptionServiceTest {

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService();
    }

    @Test
    void testCanCreateTask_PremiumUser_ShouldAllowUnlimitedTasks() {
        User premiumUser =
                User.builder()
                        .id("premium-user")
                        .subscriptionTier(User.SubscriptionTier.PREMIUM)
                        .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                        .build();

        assertTrue(subscriptionService.canCreateTask(premiumUser, 100));
        assertTrue(subscriptionService.canCreateTask(premiumUser, 1000));
    }

    @Test
    void testCanCreateTask_FreeUser_ShouldEnforceLimit() {
        User freeUser =
                User.builder()
                        .id("free-user")
                        .subscriptionTier(User.SubscriptionTier.FREE)
                        .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                        .build();

        // Should allow up to 4 tasks (5th task is allowed)
        assertTrue(subscriptionService.canCreateTask(freeUser, 0));
        assertTrue(subscriptionService.canCreateTask(freeUser, 4));

        // Should block 6th task
        assertFalse(subscriptionService.canCreateTask(freeUser, 5));
        assertFalse(subscriptionService.canCreateTask(freeUser, 10));
    }

    @Test
    void testCanCreateTask_NullUser_ShouldReturnFalse() {
        assertFalse(subscriptionService.canCreateTask(null, 0));
    }

    @Test
    void testCanCreateTask_NullTier_ShouldReturnFalse() {
        User userWithoutTier = User.builder().id("user-no-tier").build();

        assertFalse(subscriptionService.canCreateTask(userWithoutTier, 0));
    }

    @Test
    void testCanUseAI_PremiumUser_ShouldReturnTrue() {
        User premiumUser = User.builder().subscriptionTier(User.SubscriptionTier.PREMIUM).build();

        assertTrue(subscriptionService.canUseAI(premiumUser));
    }

    @Test
    void testCanUseAI_FreeUser_ShouldReturnFalse() {
        User freeUser = User.builder().subscriptionTier(User.SubscriptionTier.FREE).build();

        assertFalse(subscriptionService.canUseAI(freeUser));
    }

    @Test
    void testCanUseAI_NullUser_ShouldReturnFalse() {
        assertFalse(subscriptionService.canUseAI(null));
    }

    @Test
    void testCanAccessGiftCardStore_PremiumUser_ShouldReturnTrue() {
        User premiumUser = User.builder().subscriptionTier(User.SubscriptionTier.PREMIUM).build();

        assertTrue(subscriptionService.canAccessGiftCardStore(premiumUser));
    }

    @Test
    void testCanAccessGiftCardStore_FreeUser_ShouldReturnFalse() {
        User freeUser = User.builder().subscriptionTier(User.SubscriptionTier.FREE).build();

        assertFalse(subscriptionService.canAccessGiftCardStore(freeUser));
    }

    @Test
    void testCanAddChild_FreeUser_ShouldEnforceLimit() {
        User freeUser = User.builder().subscriptionTier(User.SubscriptionTier.FREE).build();

        // Should allow first child
        assertTrue(subscriptionService.canAddChild(freeUser, 0));

        // Should block second child
        assertFalse(subscriptionService.canAddChild(freeUser, 1));
    }

    @Test
    void testCanAddChild_PremiumUser_ShouldAllowUnlimited() {
        User premiumUser = User.builder().subscriptionTier(User.SubscriptionTier.PREMIUM).build();

        assertTrue(subscriptionService.canAddChild(premiumUser, 0));
        assertTrue(subscriptionService.canAddChild(premiumUser, 10));
    }

    @Test
    void testGetMaxRecurringTasks_PremiumUser_ShouldReturnUnlimited() {
        User premiumUser = User.builder().subscriptionTier(User.SubscriptionTier.PREMIUM).build();

        assertEquals(-1, subscriptionService.getMaxRecurringTasks(premiumUser));
    }

    @Test
    void testGetMaxRecurringTasks_FreeUser_ShouldReturnLimit() {
        User freeUser = User.builder().subscriptionTier(User.SubscriptionTier.FREE).build();

        assertEquals(5, subscriptionService.getMaxRecurringTasks(freeUser));
    }

    @Test
    void testGetMaxRecurringTasks_NullUser_ShouldReturnZero() {
        assertEquals(0, subscriptionService.getMaxRecurringTasks(null));
    }
}
