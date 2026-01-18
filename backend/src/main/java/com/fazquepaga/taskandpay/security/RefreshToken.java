package com.fazquepaga.taskandpay.security;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refresh token entity stored in Firestore.
 * Only the SHA-256 hash of the token is stored for security.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    private String id;
    private String userId;
    private String tokenHash; // SHA-256 hash of the token
    private Instant expiresAt; // +30 days from creation
    private Instant createdAt;
    private boolean revoked;
}
