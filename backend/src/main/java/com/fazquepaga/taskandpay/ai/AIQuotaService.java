package com.fazquepaga.taskandpay.ai;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing AI usage quotas.
 * Implements daily limits per user based on subscription tier:
 * - Free: 5 suggestions/day
 * - Premium: 10 suggestions/day
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIQuotaService {

    private static final int FREE_DAILY_LIMIT = 5;
    private static final int PREMIUM_DAILY_LIMIT = 10;

    private final AIQuotaRepository aiQuotaRepository;
    private final UserRepository userRepository;

    /**
     * Checks if the user can use AI features based on their daily quota.
     *
     * @param userId the user ID
     * @return true if quota is available, false otherwise
     */
    public boolean canUseAI(String userId) {
        try {
            AIQuota quota = getOrCreateQuota(userId);
            return quota.getUsedToday() < quota.getDailyLimit();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error checking AI quota for userId={}", userId, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Records an AI usage for the user, incrementing their daily counter.
     *
     * @param userId the user ID
     */
    public void recordUsage(String userId) {
        try {
            AIQuota quota = getOrCreateQuota(userId);
            quota.setUsedToday(quota.getUsedToday() + 1);
            aiQuotaRepository.save(userId, quota);

            log.info("AI quota used: userId={}, usedToday={}, dailyLimit={}, remaining={}",
                    userId,
                    quota.getUsedToday(),
                    quota.getDailyLimit(),
                    quota.getDailyLimit() - quota.getUsedToday());
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error recording AI usage for userId={}", userId, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Gets the remaining AI quota for the user.
     *
     * @param userId the user ID
     * @return remaining quota count
     */
    public int getRemainingQuota(String userId) {
        try {
            AIQuota quota = getOrCreateQuota(userId);
            return Math.max(0, quota.getDailyLimit() - quota.getUsedToday());
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error getting remaining quota for userId={}", userId, e);
            Thread.currentThread().interrupt();
            return 0;
        }
    }

    /**
     * Gets the daily limit for the user based on subscription tier.
     *
     * @param userId the user ID
     * @return daily limit
     */
    public int getDailyLimit(String userId) {
        try {
            AIQuota quota = getOrCreateQuota(userId);
            return quota.getDailyLimit();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error getting daily limit for userId={}", userId, e);
            Thread.currentThread().interrupt();
            return FREE_DAILY_LIMIT;
        }
    }

    /**
     * Verifies the user has quota available, throwing an exception if exceeded.
     *
     * @param userId the user ID
     * @throws AIQuotaExceededException if quota is exceeded
     */
    public void verifyQuotaOrThrow(String userId) {
        if (!canUseAI(userId)) {
            int remaining = getRemainingQuota(userId);
            int limit = getDailyLimit(userId);
            log.warn("AI quota exceeded: userId={}, dailyLimit={}", userId, limit);
            throw new AIQuotaExceededException(
                    "Daily AI quota exceeded",
                    remaining,
                    limit);
        }
    }

    /**
     * Gets or creates an AI quota record for the user.
     * Handles daily reset logic by checking lastResetDate.
     */
    private AIQuota getOrCreateQuota(String userId)
            throws ExecutionException, InterruptedException {

        AIQuota quota = aiQuotaRepository.findByUserId(userId);
        LocalDate today = LocalDate.now();

        if (quota == null) {
            // Create new quota for this user
            int dailyLimit = calculateDailyLimit(userId);
            quota = AIQuota.builder()
                    .userId(userId)
                    .usedToday(0)
                    .lastResetDate(today)
                    .dailyLimit(dailyLimit)
                    .build();
            aiQuotaRepository.save(userId, quota);
            log.info("Created AI quota for userId={}, dailyLimit={}", userId, dailyLimit);
            return quota;
        }

        // Check if we need to reset for a new day
        if (quota.getLastResetDate() == null || quota.getLastResetDate().isBefore(today)) {
            int dailyLimit = calculateDailyLimit(userId);
            quota.setUsedToday(0);
            quota.setLastResetDate(today);
            quota.setDailyLimit(dailyLimit);
            aiQuotaRepository.save(userId, quota);
            log.info("Reset AI quota for userId={}: newDay, dailyLimit={}", userId, dailyLimit);
        }

        return quota;
    }

    /**
     * Calculates the daily limit based on the user's subscription tier.
     */
    private int calculateDailyLimit(String userId)
            throws ExecutionException, InterruptedException {

        User user = userRepository.findByIdSync(userId);
        if (user == null) {
            return FREE_DAILY_LIMIT;
        }

        boolean isPremium = user.getSubscriptionTier() == User.SubscriptionTier.PREMIUM
                && user.getSubscriptionStatus() == User.SubscriptionStatus.ACTIVE;

        return isPremium ? PREMIUM_DAILY_LIMIT : FREE_DAILY_LIMIT;
    }
}
