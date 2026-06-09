package com.fazquepaga.taskandpay.shared.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fazquepaga.taskandpay.identity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class LoggingContextFilterTest {

    @Mock private HttpServletRequest request;

    @Mock private HttpServletResponse response;

    @Mock private FilterChain filterChain;

    private LoggingContextFilter filter;

    @BeforeEach
    void setUp() {
        filter = new LoggingContextFilter();
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    void shouldGenerateCorrelationIdWhenMissingAndSetHeader() throws Exception {
        // Arrange
        lenient().when(request.getHeader("X-Correlation-Id")).thenReturn(null);
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/test");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        doAnswer(
                        invocation -> {
                            // Assert MDC values during filter execution
                            assertThat(MDC.get("correlationId")).isNotBlank();
                            assertThat(MDC.get("requestUri")).isEqualTo("/api/v1/test");
                            assertThat(MDC.get("clientIp")).isEqualTo("127.0.0.1");
                            assertThat(MDC.get("userId")).isEqualTo("anonymous");
                            return null;
                        })
                .when(filterChain)
                .doFilter(request, response);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setHeader(eq("X-Correlation-Id"), any(String.class));
        verify(filterChain).doFilter(request, response);
        // Verify MDC is cleared after execution
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void shouldPropagateCorrelationIdWhenPresentInHeader() throws Exception {
        // Arrange
        String existingCorrId = "test-corr-id-123";
        lenient().when(request.getHeader("X-Correlation-Id")).thenReturn(existingCorrId);
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/test");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        doAnswer(
                        invocation -> {
                            assertThat(MDC.get("correlationId")).isEqualTo(existingCorrId);
                            return null;
                        })
                .when(filterChain)
                .doFilter(request, response);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setHeader("X-Correlation-Id", existingCorrId);
        verify(filterChain).doFilter(request, response);
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void shouldExtractUserIdWhenUserIsAuthenticated() throws Exception {
        // Arrange
        User user = mock(User.class);
        lenient().when(user.getId()).thenReturn("usr_test_999");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        lenient().when(request.getHeader("X-Correlation-Id")).thenReturn(null);
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/test");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        doAnswer(
                        invocation -> {
                            assertThat(MDC.get("userId")).isEqualTo("usr_test_999");
                            return null;
                        })
                .when(filterChain)
                .doFilter(request, response);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertThat(MDC.get("userId")).isNull();
    }

    @Test
    void shouldExtractIpFromXForwardedForHeader() throws Exception {
        // Arrange
        lenient().when(request.getHeader("X-Correlation-Id")).thenReturn(null);
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/test");
        lenient()
                .when(request.getHeader("X-Forwarded-For"))
                .thenReturn("203.0.113.195, 70.41.3.18");

        doAnswer(
                        invocation -> {
                            assertThat(MDC.get("clientIp")).isEqualTo("203.0.113.195");
                            return null;
                        })
                .when(filterChain)
                .doFilter(request, response);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldExtractIpFromXRealIpHeader() throws Exception {
        // Arrange
        lenient().when(request.getHeader("X-Correlation-Id")).thenReturn(null);
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/test");
        lenient().when(request.getHeader("X-Real-IP")).thenReturn("198.51.100.1");

        doAnswer(
                        invocation -> {
                            assertThat(MDC.get("clientIp")).isEqualTo("198.51.100.1");
                            return null;
                        })
                .when(filterChain)
                .doFilter(request, response);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldClearMdcEvenWhenChainThrowsException() throws Exception {
        // Arrange
        lenient().when(request.getHeader("X-Correlation-Id")).thenReturn("corr-err");
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/test");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        doThrow(new RuntimeException("Test Filter Exception"))
                .when(filterChain)
                .doFilter(request, response);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, filterChain));

        // MDC must be cleared
        assertThat(MDC.get("correlationId")).isNull();
        assertThat(MDC.get("userId")).isNull();
        assertThat(MDC.get("requestUri")).isNull();
        assertThat(MDC.get("clientIp")).isNull();
    }
}
