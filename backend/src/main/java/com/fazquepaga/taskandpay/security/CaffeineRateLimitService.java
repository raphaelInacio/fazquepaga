package com.fazquepaga.taskandpay.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of RateLimitService using Caffeine cache and Bucket4j.
 * Provides in-memory rate limiting without requiring external storage like
 * Redis.
 */
@Service
public class CaffeineRateLimitService implements RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(CaffeineRateLimitService.class);

    private final RateLimitConfig config;

    private final Cache<String, Bucket> globalBuckets;
    private final Cache<String, Bucket> authBuckets;
    private final Cache<String, Bucket> aiBuckets;

    /**
     * Create a CaffeineRateLimitService using the provided rate limit configuration and initialize internal caches.
     *
     * Initializes separate in-memory caches for global, auth, and ai buckets with expiration and size limits derived
     * from the supplied configuration and logs the configured limits and durations.
     *
     * @param config configuration containing per-type rate limits and duration values used to build buckets and cache expirations
     */
    public CaffeineRateLimitService(RateLimitConfig config) {
        this.config = config;

        // Initialize caches with expiration to prevent memory leaks
        this.globalBuckets = Caffeine.newBuilder()
                .expireAfterAccess(config.getGlobalDurationSeconds() * 2, TimeUnit.SECONDS)
                .maximumSize(10000)
                .build();

        this.authBuckets = Caffeine.newBuilder()
                .expireAfterAccess(config.getAuthDurationSeconds() * 2, TimeUnit.SECONDS)
                .maximumSize(10000)
                .build();

        this.aiBuckets = Caffeine.newBuilder()
                .expireAfterAccess(config.getAiDurationSeconds() * 2, TimeUnit.SECONDS)
                .maximumSize(10000)
                .build();

        log.info(
                "Rate limiter initialized with limits: global={}/{}s, auth={}/{}s, ai={}/{}s",
                config.getGlobalLimit(),
                config.getGlobalDurationSeconds(),
                config.getAuthLimit(),
                config.getAuthDurationSeconds(),
                config.getAiLimit(),
                config.getAiDurationSeconds());
    }

    /**
     * Attempts to consume a single token from the rate limit bucket for the given key and type.
     *
     * @param key the identifier for the bucket (e.g., user ID, IP, or API key)
     * @param bucketType the bucket category (GLOBAL, AUTH, or AI) to select limits and duration
     * @return `true` if a token was consumed (request allowed) or if the rate limiter is disabled; `false` if no tokens were available (request limited)
     */
    @Override
    public boolean tryConsume(String key, BucketType bucketType) {
        if (!config.isEnabled()) {
            return true;
        }

        Bucket bucket = getBucket(key, bucketType);
        boolean consumed = bucket.tryConsume(1);

        if (!consumed) {
            log.warn(
                    "Rate limit exceeded: key={}, bucketType={}, remaining={}",
                    key,
                    bucketType,
                    bucket.getAvailableTokens());
        }

        return consumed;
    }

    /**
     * Get the number of tokens currently available in the rate-limiting bucket for the given key and bucket type.
     *
     * If rate limiting is disabled via configuration, this returns Long.MAX_VALUE.
     *
     * @param key        the identifier used to select the bucket (per-key scope)
     * @param bucketType the bucket category (e.g., GLOBAL, AUTH, AI)
     * @return the number of available tokens in the selected bucket, or `Long.MAX_VALUE` when rate limiting is disabled
     */
    @Override
    public long getAvailableTokens(String key, BucketType bucketType) {
        if (!config.isEnabled()) {
            return Long.MAX_VALUE;
        }
        return getBucket(key, bucketType).getAvailableTokens();
    }

    /**
     * Get the configured refill interval in seconds for the specified bucket.
     *
     * @param key        identifier for the rate-limited subject (used to locate its bucket)
     * @param bucketType the bucket category (`GLOBAL`, `AUTH`, or `AI`)
     * @return `0` if rate limiting is disabled; otherwise the configured duration in seconds for the given bucket type
     */
    @Override
    public long getSecondsUntilRefill(String key, BucketType bucketType) {
        if (!config.isEnabled()) {
            return 0;
        }

        Bucket bucket = getBucket(key, bucketType);
        // Estimate time until refill based on bucket configuration
        return switch (bucketType) {
            case GLOBAL -> config.getGlobalDurationSeconds();
            case AUTH -> config.getAuthDurationSeconds();
            case AI -> config.getAiDurationSeconds();
        };
    }

    /**
     * Retrieve or create the token bucket associated with the given key and bucket type.
     *
     * The bucket is stored in an in-memory cache keyed by "<bucketType>:<key>" and will be created
     * lazily if absent.
     *
     * @param key        the identifier for the entity being rate-limited (e.g., user ID, IP, or API key)
     * @param bucketType the category of bucket to use (GLOBAL, AUTH, or AI)
     * @return           the Bucket instance for the specified key and bucket type
     */
    private Bucket getBucket(String key, BucketType bucketType) {
        String cacheKey = bucketType.name() + ":" + key;
        Cache<String, Bucket> cache = getCacheForType(bucketType);

        return cache.get(cacheKey, k -> createBucket(bucketType));
    }

    /**
     * Selects the internal Caffeine cache corresponding to the given bucket type.
     *
     * @param bucketType the bucket category (GLOBAL, AUTH, or AI) whose cache should be returned
     * @return the cache that stores Buckets for the specified bucket type
     */
    private Cache<String, Bucket> getCacheForType(BucketType bucketType) {
        return switch (bucketType) {
            case GLOBAL -> globalBuckets;
            case AUTH -> authBuckets;
            case AI -> aiBuckets;
        };
    }

    /**
     * Create a Bucket configured for the specified bucket type using limits and refill intervals from the service config.
     *
     * @param bucketType the bucket category (GLOBAL, AUTH, or AI) whose configured capacity and refill interval will be used
     * @return a Bucket containing a single Bandwidth whose capacity equals the configured limit for the bucket type and that refills at the configured interval in seconds
     */
    private Bucket createBucket(BucketType bucketType) {
        Bandwidth bandwidth = switch (bucketType) {
            case GLOBAL -> Bandwidth.classic(
                    config.getGlobalLimit(),
                    Refill.intervally(
                            config.getGlobalLimit(),
                            Duration.ofSeconds(config.getGlobalDurationSeconds())));
            case AUTH -> Bandwidth.classic(
                    config.getAuthLimit(),
                    Refill.intervally(
                            config.getAuthLimit(),
                            Duration.ofSeconds(config.getAuthDurationSeconds())));
            case AI -> Bandwidth.classic(
                    config.getAiLimit(),
                    Refill.intervally(
                            config.getAiLimit(),
                            Duration.ofSeconds(config.getAiDurationSeconds())));
        };

        return Bucket.builder().addLimit(bandwidth).build();
    }
}