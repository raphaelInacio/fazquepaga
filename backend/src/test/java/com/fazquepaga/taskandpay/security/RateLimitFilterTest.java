package com.fazquepaga.taskandpay.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RateLimitConfig config;
    private RateLimitFilter rateLimitFilter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        config = new RateLimitConfig();
        config.setEnabled(true);
        config.setGlobalLimit(100);
        config.setGlobalDurationSeconds(60);
        config.setAuthLimit(10);
        config.setAuthDurationSeconds(60);
        config.setAiLimit(5);
        config.setAiDurationSeconds(60);

        objectMapper = new ObjectMapper();
        rateLimitFilter = new RateLimitFilter(rateLimitService, config, objectMapper);
    }

    @Test
    void shouldAllowRequest_whenUnderLimit() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimitService.tryConsume("192.168.1.1", RateLimitService.BucketType.GLOBAL))
                .thenReturn(true);
        when(rateLimitService.getAvailableTokens("192.168.1.1", RateLimitService.BucketType.GLOBAL))
                .thenReturn(99L);
        when(rateLimitService.getSecondsUntilRefill(
                "192.168.1.1", RateLimitService.BucketType.GLOBAL))
                .thenReturn(60L);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response).setHeader("X-RateLimit-Limit", "100");
        verify(response).setHeader("X-RateLimit-Remaining", "99");
    }

    @Test
    void shouldReturn429_whenRateLimitExceeded() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getRemoteAddr()).thenReturn("192.168.1.2");
        when(rateLimitService.tryConsume("192.168.1.2", RateLimitService.BucketType.AUTH))
                .thenReturn(false);
        when(rateLimitService.getSecondsUntilRefill("192.168.1.2", RateLimitService.BucketType.AUTH))
                .thenReturn(30L);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, never()).doFilter(any(), any());
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setHeader("X-RateLimit-Limit", "10");
        verify(response).setHeader("X-RateLimit-Remaining", "0");
        verify(response).setHeader("Retry-After", "30");
    }

    @Test
    void shouldUseAuthLimit_forAuthEndpoints() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/auth/register");
        when(request.getRemoteAddr()).thenReturn("192.168.1.3");
        when(rateLimitService.tryConsume("192.168.1.3", RateLimitService.BucketType.AUTH))
                .thenReturn(true);
        when(rateLimitService.getAvailableTokens("192.168.1.3", RateLimitService.BucketType.AUTH))
                .thenReturn(9L);
        when(rateLimitService.getSecondsUntilRefill("192.168.1.3", RateLimitService.BucketType.AUTH))
                .thenReturn(60L);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimitService).tryConsume("192.168.1.3", RateLimitService.BucketType.AUTH);
        verify(response).setHeader("X-RateLimit-Limit", "10");
    }

    @Test
    void shouldUseAiLimit_forAiEndpoints() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/tasks/suggest");
        when(request.getRemoteAddr()).thenReturn("192.168.1.4");
        when(rateLimitService.tryConsume("192.168.1.4", RateLimitService.BucketType.AI))
                .thenReturn(true);
        when(rateLimitService.getAvailableTokens("192.168.1.4", RateLimitService.BucketType.AI))
                .thenReturn(4L);
        when(rateLimitService.getSecondsUntilRefill("192.168.1.4", RateLimitService.BucketType.AI))
                .thenReturn(60L);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimitService).tryConsume("192.168.1.4", RateLimitService.BucketType.AI);
        verify(response).setHeader("X-RateLimit-Limit", "5");
    }

    @Test
    void shouldUseChildLoginAsAuthEndpoint() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/children/login");
        when(request.getRemoteAddr()).thenReturn("192.168.1.5");
        when(rateLimitService.tryConsume("192.168.1.5", RateLimitService.BucketType.AUTH))
                .thenReturn(true);
        when(rateLimitService.getAvailableTokens("192.168.1.5", RateLimitService.BucketType.AUTH))
                .thenReturn(9L);
        when(rateLimitService.getSecondsUntilRefill("192.168.1.5", RateLimitService.BucketType.AUTH))
                .thenReturn(60L);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimitService).tryConsume("192.168.1.5", RateLimitService.BucketType.AUTH);
    }

    @Test
    void shouldExtractIpFromXForwardedForHeader() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");
        when(rateLimitService.tryConsume("10.0.0.1", RateLimitService.BucketType.GLOBAL))
                .thenReturn(true);
        when(rateLimitService.getAvailableTokens("10.0.0.1", RateLimitService.BucketType.GLOBAL))
                .thenReturn(99L);
        when(rateLimitService.getSecondsUntilRefill("10.0.0.1", RateLimitService.BucketType.GLOBAL))
                .thenReturn(60L);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimitService).tryConsume("10.0.0.1", RateLimitService.BucketType.GLOBAL);
    }

    @Test
    void shouldSkipRateLimiting_whenDisabled() throws Exception {
        // Arrange
        config.setEnabled(false);
        rateLimitFilter = new RateLimitFilter(rateLimitService, config, objectMapper);

        when(request.getRequestURI()).thenReturn("/api/v1/users");

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimitService, never()).tryConsume(any(), any());
    }

    @Test
    void shouldIncludeErrorBody_whenRateLimitExceeded() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getRemoteAddr()).thenReturn("192.168.1.6");
        when(rateLimitService.tryConsume("192.168.1.6", RateLimitService.BucketType.AUTH))
                .thenReturn(false);
        when(rateLimitService.getSecondsUntilRefill("192.168.1.6", RateLimitService.BucketType.AUTH))
                .thenReturn(45L);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        writer.flush();
        String responseBody = stringWriter.toString();
        assertThat(responseBody).contains("Too Many Requests");
        assertThat(responseBody).contains("/api/v1/auth/login");
    }
}
