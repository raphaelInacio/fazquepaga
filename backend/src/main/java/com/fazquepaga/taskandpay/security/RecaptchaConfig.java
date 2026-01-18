package com.fazquepaga.taskandpay.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Google reCAPTCHA v3.
 */
@Configuration
@ConfigurationProperties(prefix = "recaptcha")
public class RecaptchaConfig {

    private boolean enabled = true;
    private String siteKey;
    private String secretKey;
    private float threshold = 0.5f;
    private String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";

    /**
     * Indicates whether Google reCAPTCHA verification is enabled.
     *
     * @return true if reCAPTCHA is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable reCAPTCHA verification.
     *
     * @param enabled true to enable reCAPTCHA verification, false to disable it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the configured reCAPTCHA site key.
     *
     * @return the reCAPTCHA site key, or {@code null} if not configured
     */
    public String getSiteKey() {
        return siteKey;
    }

    /**
     * Set the Google reCAPTCHA v3 site key used by the application.
     *
     * @param siteKey the public site key (site key) issued by Google reCAPTCHA; may be {@code null} to unset
     */
    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    /**
     * Retrieves the reCAPTCHA secret key used for server-side verification.
     *
     * @return the secret key, or {@code null} if not configured
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Set the reCAPTCHA secret key used for server-side verification.
     *
     * @param secretKey the secret key provided by Google reCAPTCHA for this application
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * The minimum score required to consider a reCAPTCHA v3 response valid.
     *
     * @return the configured threshold score (0.0 to 1.0)
     */
    public float getThreshold() {
        return threshold;
    }

    /**
     * Set the minimum reCAPTCHA score required to consider a verification response valid.
     *
     * @param threshold the minimum score between 0.0 and 1.0; scores greater than or equal to this value are considered passing
     */
    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    /**
     * Provides the reCAPTCHA verification endpoint URL.
     *
     * @return the verification URL used to validate reCAPTCHA tokens
     */
    public String getVerifyUrl() {
        return verifyUrl;
    }

    /**
     * Set the URL used to verify Google reCAPTCHA responses.
     *
     * @param verifyUrl the verification endpoint URL (for example, "https://www.google.com/recaptcha/api/siteverify")
     */
    public void setVerifyUrl(String verifyUrl) {
        this.verifyUrl = verifyUrl;
    }
}