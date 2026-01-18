package com.fazquepaga.taskandpay.security;

/**
 * Service interface for rate limiting functionality.
 * Provides methods to check and consume rate limit tokens.
 */
public interface RateLimitService {

    /**
     * Attempts to consume tokens from the bucket associated with the given key.
     *
     * @param key        The identifier for the rate limit bucket (e.g., IP address,
     *                   user ID)
     * @param bucketType The type of bucket to use (global, auth, ai)
     * @return true if tokens were consumed successfully, false if rate limit
     *         exceeded
     */
    boolean tryConsume(String key, BucketType bucketType);

    /**
     * Gets the number of available tokens for the given key.
     *
     * @param key        The identifier for the rate limit bucket
     * @param bucketType The type of bucket
     * @return The number of available tokens
     */
    long getAvailableTokens(String key, BucketType bucketType);

    /**
     * Gets the time in seconds until the bucket refills.
     *
     * @param key        The identifier for the rate limit bucket
     * @param bucketType The type of bucket
     * @return Seconds until next refill
     */
    long getSecondsUntilRefill(String key, BucketType bucketType);

    /**
     * Enumeration of bucket types for different rate limiting scenarios.
     */
    enum BucketType {
        GLOBAL,
        AUTH,
        AI
    }
}
