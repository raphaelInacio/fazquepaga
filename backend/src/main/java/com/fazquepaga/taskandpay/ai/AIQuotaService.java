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
     * Retrieves the number of remaining AI usage operations available to the user for today.
     *
     * @param userId the user's unique identifier
     * @return the number of remaining uses for today, or 0 if none remain or an error occurs
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
     * Determine the user's daily AI usage limit based on their subscription tier.
     *
     * If an internal error occurs while retrieving quota information, the method interrupts
     * the current thread and returns FREE_DAILY_LIMIT as a safe fallback.
     *
     * @param userId the identifier of the user
     * @return the user's daily AI usage limit (FREE_DAILY_LIMIT or PREMIUM_DAILY_LIMIT)
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
     * Ensure the user has remaining AI quota for today.
     *
     * @param userId the ID of the user to check
     * @throws AIQuotaExceededException if the user has zero remaining daily AI uses
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
     * Retrieve the user's AIQuota record or create and initialize one if it does not exist.
     *
     * If a new record is created it will be initialized with usedToday = 0, lastResetDate = today,
     * and a daily limit computed from the user's subscription. If an existing record's lastResetDate
     * is null or before today, this method resets usedToday to 0, updates lastResetDate to today,
     * recalculates dailyLimit, and persists the updated record.
     *
     * @param userId the identifier of the user whose quota is requested
     * @return the existing or newly created AIQuota for the given user
     * @throws ExecutionException if a repository or user lookup operation fails
     * @throws InterruptedException if the current thread is interrupted while performing lookup or calculations
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
                    .lastResetDate(today.toString())
                    .dailyLimit(dailyLimit)
                    .build();
            aiQuotaRepository.save(userId, quota);
            log.info("Created AI quota for userId={}, dailyLimit={}", userId, dailyLimit);
            return quota;
        }

        // Check if we need to reset for a new day
        LocalDate lastReset = quota.getLastResetDate() != null ? LocalDate.parse(quota.getLastResetDate()) : null;

        if (lastReset == null || lastReset.isBefore(today)) {
            int dailyLimit = calculateDailyLimit(userId);
            quota.setUsedToday(0);
            quota.setLastResetDate(today.toString());
            quota.setDailyLimit(dailyLimit);
            aiQuotaRepository.save(userId, quota);
            log.info("Reset AI quota for userId={}: newDay, dailyLimit={}", userId, dailyLimit);
        }

        return quota;
    }

    /**
     * Determine the user's daily AI usage limit based on subscription status.
     *
     * Returns `PREMIUM_DAILY_LIMIT` for users with an active `PREMIUM` subscription; returns
     * `FREE_DAILY_LIMIT` if the user is not found or does not have an active premium subscription.
     *
     * @return `PREMIUM_DAILY_LIMIT` if the user has an active premium subscription, `FREE_DAILY_LIMIT` otherwise
     * @throws ExecutionException   if an error occurs while accessing the user repository
     * @throws InterruptedException if the thread is interrupted while retrieving the user
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