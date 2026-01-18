package com.fazquepaga.taskandpay.ai;

/**
 * Exception thrown when a user exceeds their daily AI quota.
 */
public class AIQuotaExceededException extends RuntimeException {

    private final int remainingQuota;
    private final int dailyLimit;

    /**
     * Constructs an AIQuotaExceededException with the given detail message and sets remaining quota and daily limit to 0.
     *
     * @param message the detail message describing the quota breach
     */
    public AIQuotaExceededException(String message) {
        super(message);
        this.remainingQuota = 0;
        this.dailyLimit = 0;
    }

    /**
     * Constructs an AIQuotaExceededException with a detail message and the quota state.
     *
     * @param message        a detail message describing the quota breach
     * @param remainingQuota the user's remaining quota at the time of the exception
     * @param dailyLimit     the configured daily quota limit for the user
     */
    public AIQuotaExceededException(String message, int remainingQuota, int dailyLimit) {
        super(message);
        this.remainingQuota = remainingQuota;
        this.dailyLimit = dailyLimit;
    }

    /**
     * Gets the remaining quota captured when the exception was created.
     *
     * @return the remaining quota value (number of allowed AI requests)
     */
    public int getRemainingQuota() {
        return remainingQuota;
    }

    /**
     * Gets the configured daily AI quota limit for the user.
     *
     * @return the daily quota limit as the maximum allowed AI requests per day
     */
    public int getDailyLimit() {
        return dailyLimit;
    }
}