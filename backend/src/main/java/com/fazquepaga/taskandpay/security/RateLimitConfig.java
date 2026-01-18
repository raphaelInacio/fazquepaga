package com.fazquepaga.taskandpay.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for rate limiting.
 * Allows tuning rate limits via application.properties.
 */
@Component
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitConfig {

    private boolean enabled = true;

    // Global rate limit (per IP)
    private int globalLimit = 100;
    private int globalDurationSeconds = 60;

    // Auth endpoints rate limit (per IP)
    private int authLimit = 10;
    private int authDurationSeconds = 60;

    // AI endpoints rate limit (per user)
    private int aiLimit = 5;
    private int aiDurationSeconds = 60;

    /**
     * Indicates whether rate limiting is enabled.
     *
     * @return true if rate limiting is enabled, false otherwise.
     */

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables rate limiting.
     *
     * @param enabled true to enable rate limiting, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Retrieve the global rate limit applied per IP address.
     *
     * @return the maximum number of requests allowed per IP in the global time window
     */
    public int getGlobalLimit() {
        return globalLimit;
    }

    /**
     * Set the global per-IP request limit used by the rate limiter.
     *
     * @param globalLimit the maximum number of requests allowed per IP within the configured global duration window
     */
    public void setGlobalLimit(int globalLimit) {
        this.globalLimit = globalLimit;
    }

    /**
     * Gets the time window, in seconds, used for the global rate limit.
     *
     * @return the global rate-limit duration in seconds
     */
    public int getGlobalDurationSeconds() {
        return globalDurationSeconds;
    }

    /**
     * Set the time window, in seconds, used to evaluate the global per-IP rate limit.
     *
     * @param globalDurationSeconds duration in seconds for the global rate-limit window
     */
    public void setGlobalDurationSeconds(int globalDurationSeconds) {
        this.globalDurationSeconds = globalDurationSeconds;
    }

    /**
     * Provides the configured rate limit per IP for authentication endpoints.
     *
     * @return the number of requests allowed for auth endpoints within the configured duration
     */
    public int getAuthLimit() {
        return authLimit;
    }

    /**
     * Sets the per-IP request limit for authentication endpoints.
     *
     * @param authLimit the maximum number of requests allowed within the auth duration window for a single IP
     */
    public void setAuthLimit(int authLimit) {
        this.authLimit = authLimit;
    }

    /**
     * Duration in seconds for the rate limit window applied to authentication endpoints.
     *
     * @return the time window in seconds used for authentication rate limiting
     */
    public int getAuthDurationSeconds() {
        return authDurationSeconds;
    }

    /**
     * Set the time window, in seconds, used when applying the rate limit for authentication endpoints.
     *
     * @param authDurationSeconds the duration of the auth rate-limit window in seconds
     */
    public void setAuthDurationSeconds(int authDurationSeconds) {
        this.authDurationSeconds = authDurationSeconds;
    }

    /**
     * Get the configured per-user request limit for AI endpoints.
     *
     * @return the number of requests allowed per user for AI endpoints within the configured time window.
     */
    public int getAiLimit() {
        return aiLimit;
    }

    /**
     * Sets the per-user request limit for AI endpoints.
     *
     * @param aiLimit the number of requests a single user is allowed within the AI rate limit window (aiDurationSeconds)
     */
    public void setAiLimit(int aiLimit) {
        this.aiLimit = aiLimit;
    }

    /**
     * Gets the time window in seconds used for the per-user AI endpoints rate limit.
     *
     * @return the AI endpoints rate-limit duration in seconds
     */
    public int getAiDurationSeconds() {
        return aiDurationSeconds;
    }

    /**
     * Set the time window, in seconds, used to evaluate the AI endpoints' per-user rate limit.
     *
     * @param aiDurationSeconds the duration of the AI rate-limit window in seconds
     */
    public void setAiDurationSeconds(int aiDurationSeconds) {
        this.aiDurationSeconds = aiDurationSeconds;
    }
}