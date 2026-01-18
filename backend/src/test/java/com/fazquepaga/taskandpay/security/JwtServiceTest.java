package com.fazquepaga.taskandpay.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final String TEST_SECRET = "746573742D7365637265742D6B65792D666F722D756E69742D74657374732D6D757374";
    private static final int TEST_CHILD_TTL_DAYS = 30;

    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "childTokenTtlDays", TEST_CHILD_TTL_DAYS);
    }

    @Test
    void shouldGenerateTokenWithIatClaim() {
        when(userDetails.getUsername()).thenReturn("test-user");

        String token = jwtService.generateToken(userDetails);

        Claims claims = parseToken(token);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
    }

    @Test
    void shouldGenerateParentTokenWith24HourExpiry() {
        when(userDetails.getUsername()).thenReturn("test-user");

        String token = jwtService.generateToken(userDetails);

        Claims claims = parseToken(token);
        long expirationMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        long expectedMs = 1000L * 60 * 60 * 24; // 24 hours

        // Allow 1 second tolerance for test execution time
        assertThat(expirationMs).isBetween(expectedMs - 1000, expectedMs + 1000);
    }

    @Test
    void shouldGenerateChildTokenWith30DayExpiry() {
        String token = jwtService.generateToken("child-id", "child-name", "CHILD");

        Claims claims = parseToken(token);
        long expirationMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        long expectedMs = 1000L * 60 * 60 * 24 * TEST_CHILD_TTL_DAYS; // 30 days

        // Allow 1 second tolerance for test execution time
        assertThat(expirationMs).isBetween(expectedMs - 1000, expectedMs + 1000);
    }

    @Test
    void shouldIncludeRoleClaimInChildToken() {
        String token = jwtService.generateToken("child-id", "child-name", "CHILD");

        Claims claims = parseToken(token);
        assertThat(claims.get("role", String.class)).isEqualTo("CHILD");
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        when(userDetails.getUsername()).thenReturn("test-user");

        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void shouldValidateChildTokenSuccessfully() {
        String childId = "child-123";
        String token = jwtService.generateToken(childId, "child-name", "CHILD");

        assertThat(jwtService.isTokenValid(token, childId)).isTrue();
    }

    @Test
    void shouldExtractUsernameFromToken() {
        when(userDetails.getUsername()).thenReturn("test-user");

        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("test-user");
    }

    @Test
    void shouldRejectTokenWithWrongUser() {
        when(userDetails.getUsername()).thenReturn("test-user");

        String token = jwtService.generateToken(userDetails);

        when(userDetails.getUsername()).thenReturn("different-user");
        assertThat(jwtService.isTokenValid(token, userDetails)).isFalse();
    }

    private Claims parseToken(String token) {
        byte[] keyBytes = Decoders.BASE64.decode(TEST_SECRET);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
