package com.fazquepaga.taskandpay.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that applies rate limiting to incoming requests.
 * Uses different limits for global, authentication, and AI endpoints.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitService rateLimitService;
    private final RateLimitConfig config;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher;

    // Auth endpoint patterns (stricter limits)
    private static final String[] AUTH_PATTERNS = {
            "/api/v1/auth/**", "/api/v1/children/login"
    };

    // AI endpoint patterns (quota-based limits)
    private static final String[] AI_PATTERNS = {
            "/api/v1/ai/tasks/suggestions", "/api/v1/ai/goal-coach", "/api/v1/ai/adventure-mode/tasks"
    };

    public RateLimitFilter(
            RateLimitService rateLimitService, RateLimitConfig config, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.config = config;
        this.objectMapper = objectMapper;
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!config.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String path = request.getRequestURI();
        RateLimitService.BucketType bucketType = determineBucketType(path);

        // For AI endpoints, use user ID if available; otherwise fall back to IP
        String key = clientIp;
        if (bucketType == RateLimitService.BucketType.AI) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
                key = auth.getName();
            }
        }

        // Try to consume a token
        if (!rateLimitService.tryConsume(key, bucketType)) {
            handleRateLimitExceeded(request, response, key, bucketType);
            return;
        }

        // Add rate limit headers to response
        addRateLimitHeaders(response, key, bucketType);

        filterChain.doFilter(request, response);
    }

    private RateLimitService.BucketType determineBucketType(String path) {
        // Check if it's an auth endpoint
        for (String pattern : AUTH_PATTERNS) {
            if (pathMatcher.match(pattern, path)) {
                return RateLimitService.BucketType.AUTH;
            }
        }

        // Check if it's an AI endpoint
        for (String pattern : AI_PATTERNS) {
            if (pathMatcher.match(pattern, path)) {
                return RateLimitService.BucketType.AI;
            }
        }

        // Default to global
        return RateLimitService.BucketType.GLOBAL;
    }

    private void handleRateLimitExceeded(
            HttpServletRequest request,
            HttpServletResponse response,
            String key,
            RateLimitService.BucketType bucketType)
            throws IOException {

        long retryAfter = rateLimitService.getSecondsUntilRefill(key, bucketType);
        long limit = getLimit(bucketType);

        log.warn(
                "Rate limit exceeded: ip={}, path={}, bucketType={}, key={}",
                getClientIp(request),
                request.getRequestURI(),
                bucketType,
                key);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Add rate limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader(
                "X-RateLimit-Reset", String.valueOf(Instant.now().getEpochSecond() + retryAfter));
        response.setHeader("Retry-After", String.valueOf(retryAfter));

        // Write error response body
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now().toString());
        errorBody.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        errorBody.put("error", "Too Many Requests");
        errorBody.put(
                "message",
                "Rate limit exceeded. Please wait " + retryAfter + " seconds before retrying.");
        errorBody.put("path", request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }

    private void addRateLimitHeaders(
            HttpServletResponse response, String key, RateLimitService.BucketType bucketType) {
        long limit = getLimit(bucketType);
        long remaining = rateLimitService.getAvailableTokens(key, bucketType);
        long resetTime = Instant.now().getEpochSecond()
                + rateLimitService.getSecondsUntilRefill(key, bucketType);

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
    }

    private long getLimit(RateLimitService.BucketType bucketType) {
        return switch (bucketType) {
            case GLOBAL -> config.getGlobalLimit();
            case AUTH -> config.getAuthLimit();
            case AI -> config.getAiLimit();
        };
    }

    private String getClientIp(HttpServletRequest request) {
        // Check for X-Forwarded-For header (for proxied requests)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain
            return xForwardedFor.split(",")[0].trim();
        }

        // Check for X-Real-IP header
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to remote address
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip rate limiting for static resources and actuator endpoints
        return path.startsWith("/assets/")
                || path.startsWith("/actuator/")
                || path.endsWith(".js")
                || path.endsWith(".css")
                || path.endsWith(".ico")
                || path.endsWith(".png")
                || path.endsWith(".svg")
                || path.endsWith(".json")
                || path.equals("/")
                || path.equals("/index.html")
                || path.startsWith("/api/v1/webhooks/");
    }
}
