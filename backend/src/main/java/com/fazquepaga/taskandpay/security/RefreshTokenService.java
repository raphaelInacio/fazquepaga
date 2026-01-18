package com.fazquepaga.taskandpay.security;

import java.util.Optional;

/**
 * Service for managing refresh tokens.
 */
public interface RefreshTokenService {

    /**
     * Creates a new refresh token for the given user.
     *
     * @param userId the user ID
     * @return the opaque refresh token (not the hash)
     */
    String createRefreshToken(String userId);

    /**
     * Validates a refresh token and returns a new access token if valid.
     *
     * @param refreshToken the opaque refresh token
     * @return new access token if valid, empty if invalid/expired/revoked
     */
    Optional<String> validateAndRefresh(String refreshToken);

    /**
     * Revokes all refresh tokens for a user (global logout).
     *
     * @param userId the user ID
     */
    void revokeAllTokens(String userId);
}
