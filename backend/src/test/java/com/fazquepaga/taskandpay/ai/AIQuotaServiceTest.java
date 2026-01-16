package com.fazquepaga.taskandpay.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AIQuotaServiceTest {

    @Mock
    private AIQuotaRepository aiQuotaRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AIQuotaService aiQuotaService;

    private static final String USER_ID = "user-123";

    @Test
    void shouldAllowUsage_WhenQuotaAvailable() throws ExecutionException, InterruptedException {
        // Given: Free user with 2 of 5 used
        AIQuota quota = AIQuota.builder()
                .userId(USER_ID)
                .usedToday(2)
                .lastResetDate(LocalDate.now())
                .dailyLimit(5)
                .build();
        when(aiQuotaRepository.findByUserId(USER_ID)).thenReturn(quota);

        // When
        boolean canUse = aiQuotaService.canUseAI(USER_ID);

        // Then
        assertTrue(canUse);
    }

    @Test
    void shouldBlockUsage_WhenQuotaExceeded() throws ExecutionException, InterruptedException {
        // Given: Free user with 5 of 5 used (quota exhausted)
        AIQuota quota = AIQuota.builder()
                .userId(USER_ID)
                .usedToday(5)
                .lastResetDate(LocalDate.now())
                .dailyLimit(5)
                .build();
        when(aiQuotaRepository.findByUserId(USER_ID)).thenReturn(quota);

        // When
        boolean canUse = aiQuotaService.canUseAI(USER_ID);

        // Then
        assertFalse(canUse);
    }

    @Test
    void shouldResetQuota_OnNewDay() throws ExecutionException, InterruptedException {
        // Given: User with exhausted quota from yesterday
        AIQuota quota = AIQuota.builder()
                .userId(USER_ID)
                .usedToday(5)
                .lastResetDate(LocalDate.now().minusDays(1))
                .dailyLimit(5)
                .build();
        when(aiQuotaRepository.findByUserId(USER_ID)).thenReturn(quota);

        User freeUser = User.builder()
                .id(USER_ID)
                .subscriptionTier(User.SubscriptionTier.FREE)
                .build();
        when(userRepository.findByIdSync(USER_ID)).thenReturn(freeUser);

        // When
        boolean canUse = aiQuotaService.canUseAI(USER_ID);

        // Then: quota should be reset, so usage should be allowed
        assertTrue(canUse);

        // Verify quota was saved with reset values
        ArgumentCaptor<AIQuota> quotaCaptor = ArgumentCaptor.forClass(AIQuota.class);
        verify(aiQuotaRepository).save(eq(USER_ID), quotaCaptor.capture());
        AIQuota savedQuota = quotaCaptor.getValue();
        assertEquals(0, savedQuota.getUsedToday());
        assertEquals(LocalDate.now(), savedQuota.getLastResetDate());
    }

    @Test
    void shouldIncrementUsage_WhenRecordUsage() throws ExecutionException, InterruptedException {
        // Given: User with 2 of 5 used
        AIQuota quota = AIQuota.builder()
                .userId(USER_ID)
                .usedToday(2)
                .lastResetDate(LocalDate.now())
                .dailyLimit(5)
                .build();
        when(aiQuotaRepository.findByUserId(USER_ID)).thenReturn(quota);

        // When
        aiQuotaService.recordUsage(USER_ID);

        // Then: Usage should be incremented to 3
        ArgumentCaptor<AIQuota> quotaCaptor = ArgumentCaptor.forClass(AIQuota.class);
        verify(aiQuotaRepository).save(eq(USER_ID), quotaCaptor.capture());
        assertEquals(3, quotaCaptor.getValue().getUsedToday());
    }

    @Test
    void shouldSetCorrectLimit_ForPremiumUser() throws ExecutionException, InterruptedException {
        // Given: Premium user without existing quota
        when(aiQuotaRepository.findByUserId(USER_ID)).thenReturn(null);

        User premiumUser = User.builder()
                .id(USER_ID)
                .subscriptionTier(User.SubscriptionTier.PREMIUM)
                .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                .build();
        when(userRepository.findByIdSync(USER_ID)).thenReturn(premiumUser);

        // When
        int limit = aiQuotaService.getDailyLimit(USER_ID);

        // Then: Premium limit should be 10
        assertEquals(10, limit);
    }

    @Test
    void shouldSetCorrectLimit_ForFreeUser() throws ExecutionException, InterruptedException {
        // Given: Free user without existing quota
        when(aiQuotaRepository.findByUserId(USER_ID)).thenReturn(null);

        User freeUser = User.builder()
                .id(USER_ID)
                .subscriptionTier(User.SubscriptionTier.FREE)
                .build();
        when(userRepository.findByIdSync(USER_ID)).thenReturn(freeUser);

        // When
        int limit = aiQuotaService.getDailyLimit(USER_ID);

        // Then: Free limit should be 5
        assertEquals(5, limit);
    }

    @Test
    void shouldThrowException_WhenQuotaExceeded_OnVerify()
            throws ExecutionException, InterruptedException {
        // Given: User with exhausted quota
        AIQuota quota = AIQuota.builder()
                .userId(USER_ID)
                .usedToday(5)
                .lastResetDate(LocalDate.now())
                .dailyLimit(5)
                .build();
        when(aiQuotaRepository.findByUserId(USER_ID)).thenReturn(quota);

        // When/Then
        AIQuotaExceededException exception = assertThrows(
                AIQuotaExceededException.class,
                () -> aiQuotaService.verifyQuotaOrThrow(USER_ID));

        assertEquals(0, exception.getRemainingQuota());
        assertEquals(5, exception.getDailyLimit());
    }

    @Test
    void shouldReturnRemainingQuota() throws ExecutionException, InterruptedException {
        // Given: User with 3 of 5 used
        AIQuota quota = AIQuota.builder()
                .userId(USER_ID)
                .usedToday(3)
                .lastResetDate(LocalDate.now())
                .dailyLimit(5)
                .build();
        when(aiQuotaRepository.findByUserId(USER_ID)).thenReturn(quota);

        // When
        int remaining = aiQuotaService.getRemainingQuota(USER_ID);

        // Then
        assertEquals(2, remaining);
    }

    @Test
    void shouldCreateNewQuota_WhenNoneExists() throws ExecutionException, InterruptedException {
        // Given: No existing quota
        when(aiQuotaRepository.findByUserId(USER_ID)).thenReturn(null);

        User freeUser = User.builder()
                .id(USER_ID)
                .subscriptionTier(User.SubscriptionTier.FREE)
                .build();
        when(userRepository.findByIdSync(USER_ID)).thenReturn(freeUser);

        // When
        boolean canUse = aiQuotaService.canUseAI(USER_ID);

        // Then: Should create new quota and allow usage
        assertTrue(canUse);

        ArgumentCaptor<AIQuota> quotaCaptor = ArgumentCaptor.forClass(AIQuota.class);
        verify(aiQuotaRepository).save(eq(USER_ID), quotaCaptor.capture());
        AIQuota createdQuota = quotaCaptor.getValue();
        assertEquals(0, createdQuota.getUsedToday());
        assertEquals(5, createdQuota.getDailyLimit());
        assertEquals(LocalDate.now(), createdQuota.getLastResetDate());
    }
}
