package com.fazquepaga.taskandpay.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() throws Exception {
        refreshTokenService = new RefreshTokenServiceImpl(
                refreshTokenRepository, userRepository, jwtService);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenTtlDays", 30);
    }

    @Test
    void shouldCreateRefreshToken_withValidHash() throws Exception {
        // Given
        String userId = "user-123";
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String token = refreshTokenService.createRefreshToken(userId);

        // Then
        assertThat(token).isNotNull().isNotEmpty();

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getTokenHash()).isNotNull().isNotEmpty();
        assertThat(saved.isRevoked()).isFalse();
        assertThat(saved.getExpiresAt()).isAfter(Instant.now().plus(29, ChronoUnit.DAYS));
    }

    @Test
    void shouldValidateAndRefresh_whenTokenValid() throws Exception {
        // Given
        String rawToken = "valid-raw-token";
        String userId = "user-123";
        User user = User.builder().id(userId).name("Test User").role(User.Role.PARENT).build();

        RefreshToken storedToken = RefreshToken.builder()
                .id("token-id")
                .userId(userId)
                .tokenHash("hashed-value") // Will be matched by mock
                .expiresAt(Instant.now().plus(15, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(storedToken));
        when(userRepository.findByIdSync(userId)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("new-access-token");

        // When
        Optional<String> result = refreshTokenService.validateAndRefresh(rawToken);

        // Then
        assertThat(result).isPresent().contains("new-access-token");
        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldReturnEmpty_whenTokenNotFound() throws Exception {
        // Given
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        // When
        Optional<String> result = refreshTokenService.validateAndRefresh("unknown-token");

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, never()).findByIdSync(anyString());
    }

    @Test
    void shouldReturnEmpty_whenTokenExpired() throws Exception {
        // Given
        RefreshToken expiredToken = RefreshToken.builder()
                .id("token-id")
                .userId("user-123")
                .tokenHash("hashed-value")
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS)) // Expired
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(expiredToken));

        // When
        Optional<String> result = refreshTokenService.validateAndRefresh("expired-token");

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, never()).findByIdSync(anyString());
    }

    @Test
    void shouldReturnEmpty_whenTokenRevoked() throws Exception {
        // Given
        RefreshToken revokedToken = RefreshToken.builder()
                .id("token-id")
                .userId("user-123")
                .tokenHash("hashed-value")
                .expiresAt(Instant.now().plus(15, ChronoUnit.DAYS))
                .revoked(true) // Revoked
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(revokedToken));

        // When
        Optional<String> result = refreshTokenService.validateAndRefresh("revoked-token");

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, never()).findByIdSync(anyString());
    }

    @Test
    void shouldRevokeAllTokens_forUser() throws Exception {
        // Given
        String userId = "user-123";

        // When
        refreshTokenService.revokeAllTokens(userId);

        // Then
        verify(refreshTokenRepository).revokeAllForUser(userId);
    }

    @Test
    void shouldGenerateChildToken_whenUserIsChild() throws Exception {
        // Given
        String rawToken = "valid-raw-token";
        String userId = "child-123";
        User child = User.builder()
                .id(userId)
                .name("Child User")
                .role(User.Role.CHILD)
                .build();

        RefreshToken storedToken = RefreshToken.builder()
                .id("token-id")
                .userId(userId)
                .tokenHash("hashed-value")
                .expiresAt(Instant.now().plus(15, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(storedToken));
        when(userRepository.findByIdSync(userId)).thenReturn(child);
        when(jwtService.generateToken(userId, "Child User", "CHILD"))
                .thenReturn("new-child-access-token");

        // When
        Optional<String> result = refreshTokenService.validateAndRefresh(rawToken);

        // Then
        assertThat(result).isPresent().contains("new-child-access-token");
        verify(jwtService).generateToken(userId, "Child User", "CHILD");
    }
}
