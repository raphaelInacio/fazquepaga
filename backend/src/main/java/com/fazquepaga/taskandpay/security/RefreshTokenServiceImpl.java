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

    /**
     * Constructs a RefreshTokenServiceImpl with the required repositories and JWT service and initializes cryptographic randomness.
     *
     * @param refreshTokenRepository repository used to persist and query refresh token records
     * @param userRepository         repository used to load user information
     * @param jwtService             service used to generate access JWTs
     */
    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generate and persist a new opaque refresh token for the given user.
     *
     * @param userId the identifier of the user to associate the refresh token with
     * @return the newly generated refresh token (plain, URL-safe Base64, not the stored hash)
     * @throws RuntimeException if token persistence fails or the operation is interrupted
     */
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

    /**
     * Validate a refresh token and produce a new access token.
     *
     * @param refreshToken the opaque refresh token presented by the client
     * @return an Optional containing a newly generated access token when the refresh token exists, is not revoked, is not expired, and the user exists; `Optional.empty()` otherwise.
     *         If the associated user has role `CHILD`, the access token is generated using the user's id, name, and role; otherwise it is generated from the full user object.
     */
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

    /**
     * Revoke all stored refresh tokens for the specified user.
     *
     * @param userId the user's unique identifier whose refresh tokens will be revoked
     * @throws RuntimeException if the repository operation fails or is interrupted
     */
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
     * Compute the SHA-256 digest of the given token and return it encoded as Base64.
     *
     * @param token the plaintext token to hash
     * @return the SHA-256 hash of the token encoded as a Base64 string
     * @throws RuntimeException if the SHA-256 algorithm is unavailable
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