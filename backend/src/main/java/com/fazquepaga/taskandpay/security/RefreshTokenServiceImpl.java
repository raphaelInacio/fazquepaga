package com.fazquepaga.taskandpay.security;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Implementation of RefreshTokenService.
 * Generates opaque tokens, stores SHA-256 hashes in Firestore.
 */
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);
    private static final int TOKEN_BYTE_LENGTH = 32; // 256 bits

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final SecureRandom secureRandom;

    @Value("${jwt.refresh-token-ttl-days:30}")
    private int refreshTokenTtlDays;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public String createRefreshToken(String userId) {
        try {
            // Generate cryptographically secure random token
            byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
            secureRandom.nextBytes(tokenBytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

            // Hash the token for storage
            String tokenHash = hashToken(token);

            // Create and save the refresh token entity
            RefreshToken refreshToken = RefreshToken.builder()
                    .userId(userId)
                    .tokenHash(tokenHash)
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plus(refreshTokenTtlDays, ChronoUnit.DAYS))
                    .revoked(false)
                    .build();

            refreshTokenRepository.save(refreshToken);
            log.debug("Created refresh token for user: {}", userId);

            // Return the plain token (not the hash)
            return token;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to create refresh token for user: {}", userId, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to create refresh token", e);
        }
    }

    @Override
    public Optional<String> validateAndRefresh(String refreshToken) {
        try {
            String tokenHash = hashToken(refreshToken);
            Optional<RefreshToken> storedToken = refreshTokenRepository.findByTokenHash(tokenHash);

            if (storedToken.isEmpty()) {
                log.warn("Refresh token not found");
                return Optional.empty();
            }

            RefreshToken token = storedToken.get();

            // Check if revoked
            if (token.isRevoked()) {
                log.warn("Refresh token is revoked for user: {}", token.getUserId());
                return Optional.empty();
            }

            // Check if expired
            if (Instant.now().isAfter(token.getExpiresAt())) {
                log.warn("Refresh token is expired for user: {}", token.getUserId());
                return Optional.empty();
            }

            // Get user and generate new access token
            User user = userRepository.findByIdSync(token.getUserId());
            if (user == null) {
                log.error("User not found for refresh token: {}", token.getUserId());
                return Optional.empty();
            }
            String newAccessToken;

            if (user.getRole() == User.Role.CHILD) {
                newAccessToken = jwtService.generateToken(user.getId(), user.getName(), user.getRole().name());
            } else {
                newAccessToken = jwtService.generateToken(user);
            }

            log.debug("Refreshed access token for user: {}", token.getUserId());
            return Optional.of(newAccessToken);

        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to validate refresh token", e);
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    @Override
    public void revokeAllTokens(String userId) {
        try {
            refreshTokenRepository.revokeAllForUser(userId);
            log.info("Revoked all refresh tokens for user: {}", userId);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to revoke refresh tokens for user: {}", userId, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to revoke refresh tokens", e);
        }
    }

    /**
     * Hash a token using SHA-256.
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
