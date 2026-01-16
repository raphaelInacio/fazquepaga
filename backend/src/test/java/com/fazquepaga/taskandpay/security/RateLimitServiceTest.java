package com.fazquepaga.taskandpay.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimitServiceTest {

    private RateLimitConfig config;
    private CaffeineRateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        config = new RateLimitConfig();
        config.setEnabled(true);
        config.setGlobalLimit(5); // Small limits for testing
        config.setGlobalDurationSeconds(60);
        config.setAuthLimit(3);
        config.setAuthDurationSeconds(60);
        config.setAiLimit(2);
        config.setAiDurationSeconds(60);

        rateLimitService = new CaffeineRateLimitService(config);
    }

    @Test
    void shouldAllowRequest_whenBucketHasTokens() {
        // Arrange
        String key = "192.168.1.1";

        // Act
        boolean result = rateLimitService.tryConsume(key, RateLimitService.BucketType.GLOBAL);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldBlockRequest_whenBucketEmpty() {
        // Arrange
        String key = "192.168.1.2";

        // Exhaust all tokens
        for (int i = 0; i < config.getGlobalLimit(); i++) {
            rateLimitService.tryConsume(key, RateLimitService.BucketType.GLOBAL);
        }

        // Act
        boolean result = rateLimitService.tryConsume(key, RateLimitService.BucketType.GLOBAL);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldUseSeparateBuckets_forDifferentKeys() {
        // Arrange
        String key1 = "192.168.1.3";
        String key2 = "192.168.1.4";

        // Exhaust tokens for key1
        for (int i = 0; i < config.getGlobalLimit(); i++) {
            rateLimitService.tryConsume(key1, RateLimitService.BucketType.GLOBAL);
        }

        // Act - key2 should still have tokens
        boolean result = rateLimitService.tryConsume(key2, RateLimitService.BucketType.GLOBAL);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldUseSeparateBuckets_forDifferentBucketTypes() {
        // Arrange
        String key = "192.168.1.5";

        // Exhaust global tokens
        for (int i = 0; i < config.getGlobalLimit(); i++) {
            rateLimitService.tryConsume(key, RateLimitService.BucketType.GLOBAL);
        }

        // Act - auth bucket should still have tokens
        boolean result = rateLimitService.tryConsume(key, RateLimitService.BucketType.AUTH);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnCorrectAvailableTokens() {
        // Arrange
        String key = "192.168.1.6";
        rateLimitService.tryConsume(key, RateLimitService.BucketType.GLOBAL);
        rateLimitService.tryConsume(key, RateLimitService.BucketType.GLOBAL);

        // Act
        long available = rateLimitService.getAvailableTokens(key, RateLimitService.BucketType.GLOBAL);

        // Assert
        assertThat(available).isEqualTo(config.getGlobalLimit() - 2);
    }

    @Test
    void shouldAllowAllRequests_whenRateLimitingDisabled() {
        // Arrange
        config.setEnabled(false);
        rateLimitService = new CaffeineRateLimitService(config);
        String key = "192.168.1.7";

        // Act - try many requests
        boolean allAllowed = true;
        for (int i = 0; i < 100; i++) {
            if (!rateLimitService.tryConsume(key, RateLimitService.BucketType.GLOBAL)) {
                allAllowed = false;
                break;
            }
        }

        // Assert
        assertThat(allAllowed).isTrue();
    }

    @Test
    void shouldApplyDifferentLimits_forDifferentBucketTypes() {
        // Arrange
        String key = "192.168.1.8";

        // Act - exhaust auth bucket (limit = 3)
        for (int i = 0; i < config.getAuthLimit(); i++) {
            assertThat(rateLimitService.tryConsume(key, RateLimitService.BucketType.AUTH)).isTrue();
        }

        // Assert - next auth request should fail
        assertThat(rateLimitService.tryConsume(key, RateLimitService.BucketType.AUTH)).isFalse();

        // But AI bucket (limit = 2) should still work
        assertThat(rateLimitService.tryConsume(key, RateLimitService.BucketType.AI)).isTrue();
        assertThat(rateLimitService.tryConsume(key, RateLimitService.BucketType.AI)).isTrue();
        assertThat(rateLimitService.tryConsume(key, RateLimitService.BucketType.AI)).isFalse();
    }

    @Test
    void shouldReturnMaxValue_forAvailableTokens_whenDisabled() {
        // Arrange
        config.setEnabled(false);
        rateLimitService = new CaffeineRateLimitService(config);
        String key = "192.168.1.9";

        // Act
        long available = rateLimitService.getAvailableTokens(key, RateLimitService.BucketType.GLOBAL);

        // Assert
        assertThat(available).isEqualTo(Long.MAX_VALUE);
    }
}
