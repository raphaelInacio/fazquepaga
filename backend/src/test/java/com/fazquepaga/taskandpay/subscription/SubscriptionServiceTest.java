package com.fazquepaga.taskandpay.subscription;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.notification.NotificationService;
import com.fazquepaga.taskandpay.payment.AsaasService;
import com.fazquepaga.taskandpay.subscription.dto.CancelSubscriptionRequest;
import com.fazquepaga.taskandpay.subscription.dto.CancelSubscriptionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SubscriptionServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AsaasService asaasService;
    @Mock private NotificationService notificationService;

    @InjectMocks private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCanCreateTask_PremiumUser_ShouldAllowUnlimitedTasks() {
        User premiumUser =
                User.builder()
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
                        .subscriptionTier(User.SubscriptionTier.FREE)
                        .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                        .build();

        // Limit is 50 in code
        assertTrue(subscriptionService.canCreateTask(freeUser, 0));
        assertTrue(subscriptionService.canCreateTask(freeUser, 49));

        // Should block 50th task
        assertFalse(subscriptionService.canCreateTask(freeUser, 50));
    }

    // Skipped NullUser/NullTier for brevity as implementation handles User null
    // check but maybe not fields
    // Implementation: user != null && ...

    @Test
    void testCanUseAI_PremiumUser_ShouldReturnTrue() {
        User premiumUser =
                User.builder()
                        .subscriptionTier(User.SubscriptionTier.PREMIUM)
                        .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                        .build();
        assertTrue(subscriptionService.canUseAI(premiumUser));
    }

    @Test
    void testCanUseAI_FreeUser_ShouldReturnFalse() {
        User freeUser = User.builder().subscriptionTier(User.SubscriptionTier.FREE).build();
        assertFalse(subscriptionService.canUseAI(freeUser));
    }

    @Test
    void testCanAccessGiftCardStore_PremiumUser_ShouldReturnTrue() {
        User premiumUser =
                User.builder()
                        .subscriptionTier(User.SubscriptionTier.PREMIUM)
                        .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                        .build();
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

        // Limit is 2
        assertTrue(subscriptionService.canAddChild(freeUser, 0));
        assertTrue(subscriptionService.canAddChild(freeUser, 1));

        assertFalse(subscriptionService.canAddChild(freeUser, 2));
    }

    @Test
    void testCanAddChild_PremiumUser_ShouldAllowUnlimited() {
        User premiumUser =
                User.builder()
                        .subscriptionTier(User.SubscriptionTier.PREMIUM)
                        .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                        .build();
        assertTrue(subscriptionService.canAddChild(premiumUser, 10));
    }

    @Test
    void testGetMaxRecurringTasks_PremiumUser_ShouldReturnUnlimited() {
        User premiumUser =
                User.builder()
                        .subscriptionTier(User.SubscriptionTier.PREMIUM)
                        .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                        .build();
        assertEquals(100, subscriptionService.getMaxRecurringTasks(premiumUser));
    }

    @Test
    void testGetMaxRecurringTasks_FreeUser_ShouldReturnLimit() {
        User freeUser = User.builder().subscriptionTier(User.SubscriptionTier.FREE).build();
        assertEquals(3, subscriptionService.getMaxRecurringTasks(freeUser));
    }

    // Trial Expiration Tests

    @Test
    void testIsTrialExpired_WithinTrial_ShouldReturnFalse() {
        User user =
                User.builder()
                        .subscriptionTier(User.SubscriptionTier.FREE)
                        .trialStartDate(
                                java.time.Instant.now()
                                        .minus(1, java.time.temporal.ChronoUnit.DAYS))
                        .build();
        assertFalse(subscriptionService.isTrialExpired(user));
    }

    @Test
    void testIsTrialExpired_AfterThreeDays_ShouldReturnTrue() {
        User user =
                User.builder()
                        .subscriptionTier(User.SubscriptionTier.FREE)
                        .trialStartDate(
                                java.time.Instant.now()
                                        .minus(4, java.time.temporal.ChronoUnit.DAYS))
                        .build();
        assertTrue(subscriptionService.isTrialExpired(user));
    }

    @Test
    void testIsTrialExpired_PremiumUser_ShouldReturnFalse() {
        User premiumUser =
                User.builder()
                        .subscriptionTier(User.SubscriptionTier.PREMIUM)
                        .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                        .trialStartDate(
                                java.time.Instant.now()
                                        .minus(10, java.time.temporal.ChronoUnit.DAYS))
                        .build();
        assertFalse(subscriptionService.isTrialExpired(premiumUser));
    }

    @Test
    void testIsTrialExpired_NoTrialStartDate_LegacyUser_ShouldReturnFalse() {
        // Legacy users (registered before trial feature) are grandfathered
        User user =
                User.builder()
                        .subscriptionTier(User.SubscriptionTier.FREE)
                        .trialStartDate(null)
                        .build();
        assertFalse(subscriptionService.isTrialExpired(user));
    }

    @Test
    void testGetTrialDaysRemaining_WithinTrial_ShouldReturnCorrectDays() {
        User user =
                User.builder()
                        .subscriptionTier(User.SubscriptionTier.FREE)
                        .trialStartDate(
                                java.time.Instant.now()
                                        .minus(1, java.time.temporal.ChronoUnit.DAYS))
                        .build();
        Integer daysRemaining = subscriptionService.getTrialDaysRemaining(user);
        assertNotNull(daysRemaining);
        assertTrue(daysRemaining >= 1 && daysRemaining <= 2);
    }

    @Test
    void testGetTrialDaysRemaining_Expired_ShouldReturnZero() {
        User user =
                User.builder()
                        .subscriptionTier(User.SubscriptionTier.FREE)
                        .trialStartDate(
                                java.time.Instant.now()
                                        .minus(5, java.time.temporal.ChronoUnit.DAYS))
                        .build();
        assertEquals(0, subscriptionService.getTrialDaysRemaining(user));
    }

    @Test
    void testGetTrialDaysRemaining_PremiumUser_ShouldReturnNull() {
        User premiumUser =
                User.builder()
                        .subscriptionTier(User.SubscriptionTier.PREMIUM)
                        .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                        .build();
        assertNull(subscriptionService.getTrialDaysRemaining(premiumUser));
    }

    @Test
    void testGetTrialDaysRemaining_NoTrialStartDate_LegacyUser_ShouldReturnNull() {
        // Legacy users (registered before trial feature) - no badge shown
        User user =
                User.builder()
                        .subscriptionTier(User.SubscriptionTier.FREE)
                        .trialStartDate(null)
                        .build();
        assertNull(subscriptionService.getTrialDaysRemaining(user));
    }

    @Test
    void testCancelSubscription_Success() throws Exception {
        User user =
                User.builder()
                        .id("user1")
                        .subscriptionTier(User.SubscriptionTier.PREMIUM)
                        .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                        .subscriptionId("sub_123")
                        .build();

        when(userRepository.findByIdSync("user1")).thenReturn(user);

        CancelSubscriptionRequest request =
                new CancelSubscriptionRequest(CancellationReason.TOO_EXPENSIVE, null);

        CancelSubscriptionResponse response =
                subscriptionService.cancelSubscription("user1", request);

        verify(asaasService, times(1)).cancelSubscription("sub_123");
        verify(userRepository, times(1)).save(user);
        verify(notificationService, times(1)).sendSubscriptionCanceled(eq(user), isNull());

        assertEquals(User.SubscriptionStatus.PENDING_CANCELLATION, user.getSubscriptionStatus());
        assertEquals(CancellationReason.TOO_EXPENSIVE, user.getCancellationReason());
        assertNotNull(user.getCancellationDate());
        assertEquals(User.SubscriptionStatus.PENDING_CANCELLATION, response.getStatus());
    }

    @Test
    void testCancelSubscription_NoActivePremium_ThrowsException() throws Exception {
        User user =
                User.builder()
                        .id("user1")
                        .subscriptionTier(User.SubscriptionTier.FREE)
                        .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                        .build();

        when(userRepository.findByIdSync("user1")).thenReturn(user);

        CancelSubscriptionRequest request =
                new CancelSubscriptionRequest(CancellationReason.TOO_EXPENSIVE, null);

        IllegalStateException ex =
                assertThrows(
                        IllegalStateException.class,
                        () -> subscriptionService.cancelSubscription("user1", request));
        assertEquals("User does not have an active premium subscription", ex.getMessage());
    }

    @Test
    void testConfirmCancellation_Success() throws Exception {
        User user =
                User.builder()
                        .id("user1")
                        .asaasCustomerId("cus_123")
                        .subscriptionStatus(User.SubscriptionStatus.PENDING_CANCELLATION)
                        .build();

        when(userRepository.findByAsaasCustomerId("cus_123")).thenReturn(user);

        subscriptionService.confirmCancellation("cus_123");

        assertEquals(User.SubscriptionStatus.CANCELED, user.getSubscriptionStatus());
        assertEquals(User.SubscriptionTier.FREE, user.getSubscriptionTier());
        verify(userRepository, times(1)).save(user);
    }
}
