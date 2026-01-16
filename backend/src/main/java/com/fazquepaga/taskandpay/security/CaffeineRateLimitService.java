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

    @Override
    public long getAvailableTokens(String key, BucketType bucketType) {
        if (!config.isEnabled()) {
            return Long.MAX_VALUE;
        }
        return getBucket(key, bucketType).getAvailableTokens();
    }

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

    private Bucket getBucket(String key, BucketType bucketType) {
        String cacheKey = bucketType.name() + ":" + key;
        Cache<String, Bucket> cache = getCacheForType(bucketType);

        return cache.get(cacheKey, k -> createBucket(bucketType));
    }

    private Cache<String, Bucket> getCacheForType(BucketType bucketType) {
        return switch (bucketType) {
            case GLOBAL -> globalBuckets;
            case AUTH -> authBuckets;
            case AI -> aiBuckets;
        };
    }

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
