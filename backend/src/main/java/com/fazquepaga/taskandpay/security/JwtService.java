package com.fazquepaga.taskandpay.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final long PARENT_TOKEN_TTL_MS = 1000L * 60 * 60 * 24; // 24 hours

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.child-token-ttl-days:30}")
    private int childTokenTtlDays;

    /**
     * Extracts the username stored in the token's subject claim.
     *
     * @param token the JWT string to read the subject from
     * @return the subject (username) claim from the token, or {@code null} if the claim is absent
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Create a signed JWT with the user's username as the subject and the supplied claims.
     *
     * The token's issued-at is the current time and its expiration is current time plus PARENT_TOKEN_TTL_MS.
     *
     * @param extraClaims additional claims to include in the token payload
     * @param userDetails user whose username will be set as the token subject
     * @return the compact serialized JWT string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(
                        new Date(System.currentTimeMillis() + PARENT_TOKEN_TTL_MS)) // 24 hours
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // For Child which might not be a full UserDetails yet, or just to handle
    /**
     * Generate a signed child JWT with the given subject and role claim.
     *
     * @param userId   the user identifier placed in the token's subject
     * @param username the username associated with the user (not included in the token's claims)
     * @param role     the role value stored in the token under the `role` claim
     * @return         the compact serialized JWT signed with HS256, expiring after the configured child-token TTL
     */
    public String generateToken(String userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(
                        userId) // Using ID as subject for simplicity in our IdentityService flow?
                // Or we can use
                // phone/code
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 1000L * 60 * 60 * 24 * childTokenTtlDays)) // Configurable TTL for children
                // (simplified login)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenValid(String token, String userId) {
        final String extractedId = extractUsername(token);
        return (extractedId.equals(userId)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parses the provided JWT string using the service's signing key and returns all claims from its body.
     *
     * @param token the compact JWT string to parse
     * @return the token's claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Derives the HMAC-SHA signing key from the configured Base64-encoded secret.
     *
     * @return the HMAC-SHA key used for signing and verifying JWTs
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}