package com.fazquepaga.taskandpay.security;

/**
 * Service interface for rate limiting functionality.
 * Provides methods to check and consume rate limit tokens.
 */
public interface RateLimitService {

    /**
 * Attempt to consume one or more tokens from the rate-limit bucket for the given key and bucket type.
 *
 * @param key        identifier of the client or resource whose bucket is checked (e.g., IP, user ID)
 * @param bucketType the rate-limit bucket category to apply
 * @return `true` if tokens were consumed, `false` otherwise
 */
    boolean tryConsume(String key, BucketType bucketType);

    /**
 * Gets the number of tokens currently available for the specified key and bucket type.
 *
 * @param key        identifier for the client or bucket
 * @param bucketType rate limit bucket category
 * @return           the number of tokens currently available
 */
    long getAvailableTokens(String key, BucketType bucketType);

    /**
 * Number of seconds until the rate-limit bucket for the given key and bucket type refills.
 *
 * @param key        identifier for the client or resource whose rate-limit bucket is tracked
 * @param bucketType category of the bucket (e.g., {@code GLOBAL}, {@code AUTH}, {@code AI})
 * @return           the number of seconds until the bucket next refills
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