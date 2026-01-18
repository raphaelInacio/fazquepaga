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
 * Validate a refresh token and issue a new access token.
 *
 * @param refreshToken the opaque refresh token
 * @return an Optional containing the new access token if the refresh token is valid; empty otherwise (invalid, expired, or revoked)
 */
    Optional<String> validateAndRefresh(String refreshToken);

    /**
     * Revokes all refresh tokens for a user (global logout).
     *
     * @param userId the user ID
     */
    void revokeAllTokens(String userId);
}