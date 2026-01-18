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

    /**
     * Initialize a RateLimitFilter with its required services and JSON serializer.
     *
     * @param rateLimitService service used to consume tokens and query rate-limit state
     * @param config           rate limit configuration (limits and enabled flag)
     * @param objectMapper     JSON serializer for error responses
     */
    public RateLimitFilter(
            RateLimitService rateLimitService, RateLimitConfig config, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.config = config;
        this.objectMapper = objectMapper;
        this.pathMatcher = new AntPathMatcher();
    }

    /**
     * Enforces rate limiting for the incoming HTTP request and either continues the filter chain
     * with appropriate rate-limit headers or responds with HTTP 429 when the limit is exceeded.
     *
     * <p>Behavior:
     * - If rate limiting is disabled in configuration, the request is passed through unchanged.
     * - Determines the rate-limit bucket from the request path.
     * - Uses the client IP as the consumption key, or the authenticated user name for AI bucket requests when available.
     * - Attempts to consume a token from the appropriate bucket; on success adds rate-limit headers and proceeds,
     *   on failure writes a 429 response describing the rate limit breach.
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response to modify when adding headers or sending a 429
     * @param filterChain the filter chain to continue when the request is allowed
     * @throws ServletException if an error occurs during request processing by the filter chain
     * @throws IOException if an I/O error occurs while writing the response
     */
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

    /**
     * Classifies a request path into the rate-limit bucket that should apply.
     *
     * @param path the request URI path to classify
     * @return `AUTH` if the path matches authentication endpoint patterns, `AI` if the path matches AI endpoint patterns, `GLOBAL` otherwise
     */
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

    /**
     * Send a 429 Too Many Requests response with JSON error body and appropriate rate-limit headers when a request exceeds its bucket.
     *
     * @param request    the incoming HTTP request; used to obtain the request URI and client IP for logging and the response body
     * @param response   the HTTP response to populate with status, rate-limit headers, and the JSON error body
     * @param key        the rate-limit key (e.g., client IP or authenticated user name) associated with the exceeded bucket
     * @param bucketType the bucket type whose limits were exceeded
     * @throws IOException if writing the JSON error body to the response fails
     */
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

    /**
     * Attach rate-limit headers to the HTTP response for the given key and bucket type.
     *
     * Sets the following headers on the response:
     * - `X-RateLimit-Limit`: the configured maximum number of requests for the bucket
     * - `X-RateLimit-Remaining`: the number of remaining tokens for the key
     * - `X-RateLimit-Reset`: epoch seconds when the bucket will next be refilled
     *
     * @param response   the HTTP response to augment
     * @param key        identifier for the rate-limit bucket (for example, client IP or authenticated username)
     * @param bucketType the bucket category (GLOBAL, AUTH, or AI) whose limits apply
     */
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

    /**
     * Retrieve the configured request limit for the specified rate-limit bucket.
     *
     * @param bucketType the bucket type (GLOBAL, AUTH, or AI) whose configured limit to return
     * @return the configured request limit for the given bucket type
     */
    private long getLimit(RateLimitService.BucketType bucketType) {
        return switch (bucketType) {
            case GLOBAL -> config.getGlobalLimit();
            case AUTH -> config.getAuthLimit();
            case AI -> config.getAiLimit();
        };
    }

    /**
     * Determine the client's IP address for the given request, preferring proxy headers when present.
     *
     * @param request the HTTP servlet request to extract the client IP from
     * @return the client's IP address as a string; if `X-Forwarded-For` is present the first IP in the list is returned, otherwise `X-Real-IP` if present, or `request.getRemoteAddr()` as a fallback
     */
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

    /**
     * Determines whether the incoming request should be excluded from rate limiting.
     *
     * <p>Requests for static assets (extensions like .js, .css, .ico, .png, .svg, .json),
     * actuator endpoints, the root path ("/"), and "/index.html" are excluded.
     *
     * @return `true` if the request should not be filtered by the rate limiter, `false` otherwise
     */
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
                || path.equals("/index.html");
    }
}