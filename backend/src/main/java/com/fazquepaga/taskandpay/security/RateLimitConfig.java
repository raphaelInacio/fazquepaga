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

    // Getters and setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getGlobalLimit() {
        return globalLimit;
    }

    public void setGlobalLimit(int globalLimit) {
        this.globalLimit = globalLimit;
    }

    public int getGlobalDurationSeconds() {
        return globalDurationSeconds;
    }

    public void setGlobalDurationSeconds(int globalDurationSeconds) {
        this.globalDurationSeconds = globalDurationSeconds;
    }

    public int getAuthLimit() {
        return authLimit;
    }

    public void setAuthLimit(int authLimit) {
        this.authLimit = authLimit;
    }

    public int getAuthDurationSeconds() {
        return authDurationSeconds;
    }

    public void setAuthDurationSeconds(int authDurationSeconds) {
        this.authDurationSeconds = authDurationSeconds;
    }

    public int getAiLimit() {
        return aiLimit;
    }

    public void setAiLimit(int aiLimit) {
        this.aiLimit = aiLimit;
    }

    public int getAiDurationSeconds() {
        return aiDurationSeconds;
    }

    public void setAiDurationSeconds(int aiDurationSeconds) {
        this.aiDurationSeconds = aiDurationSeconds;
    }
}
