package com.fazquepaga.taskandpay.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RecaptchaResponse {

    private boolean success;
    private float score;
    private String action;

    @JsonProperty("challenge_ts")
    private String challengeTs;

    private String hostname;

    @JsonProperty("error-codes")
    private List<String> errorCodes;

    /**
     * Indicates whether the reCAPTCHA verification succeeded.
     *
     * @return `true` if the verification succeeded, `false` otherwise.
     */

    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets whether the reCAPTCHA verification succeeded.
     *
     * @param success `true` if verification succeeded, `false` otherwise
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Retrieves the reCAPTCHA evaluation score.
     *
     * @return the score assigned by reCAPTCHA; higher values indicate greater confidence that the interaction is legitimate
     */
    public float getScore() {
        return score;
    }

    /**
     * Set the reCAPTCHA score assigned to this response.
     *
     * @param score the score value from reCAPTCHA, expected in the range 0.0 to 1.0
     */
    public void setScore(float score) {
        this.score = score;
    }

    /**
     * The action name associated with the reCAPTCHA verification.
     *
     * @return the action name supplied with the verification, or {@code null} if absent
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the action name associated with the reCAPTCHA verification.
     *
     * @param action the action name to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * reCAPTCHA challenge timestamp from the verification response.
     *
     * @return the challenge timestamp string mapped from JSON field "challenge_ts", or {@code null} if absent
     */
    public String getChallengeTs() {
        return challengeTs;
    }

    /**
     * Set the challenge timestamp returned by reCAPTCHA (maps from JSON field `challenge_ts`).
     *
     * @param challengeTs the timestamp string provided by the reCAPTCHA verification response
     */
    public void setChallengeTs(String challengeTs) {
        this.challengeTs = challengeTs;
    }

    /**
     * Hostname from which the reCAPTCHA verification request originated.
     *
     * @return the hostname associated with the verification, or {@code null} if not present
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the hostname from which the reCAPTCHA verification was performed.
     *
     * @param hostname the hostname reported by the reCAPTCHA response
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Retrieve the reCAPTCHA verification error codes, if any.
     *
     * @return the list of error codes returned by reCAPTCHA, or null if not present.
     */
    public List<String> getErrorCodes() {
        return errorCodes;
    }

    /**
     * Sets the list of reCAPTCHA error codes returned by the verification response.
     *
     * @param errorCodes list of error code strings from the reCAPTCHA response (mapped from JSON field "error-codes"); may be null or empty if there are no errors
     */
    public void setErrorCodes(List<String> errorCodes) {
        this.errorCodes = errorCodes;
    }
}