package com.fazquepaga.taskandpay.ai;

/**
 * Exception thrown when a user exceeds their daily AI quota.
 */
public class AIQuotaExceededException extends RuntimeException {

    private final int remainingQuota;
    private final int dailyLimit;

    public AIQuotaExceededException(String message) {
        super(message);
        this.remainingQuota = 0;
        this.dailyLimit = 0;
    }

    public AIQuotaExceededException(String message, int remainingQuota, int dailyLimit) {
        super(message);
        this.remainingQuota = remainingQuota;
        this.dailyLimit = dailyLimit;
    }

    public int getRemainingQuota() {
        return remainingQuota;
    }

    public int getDailyLimit() {
        return dailyLimit;
    }
}
