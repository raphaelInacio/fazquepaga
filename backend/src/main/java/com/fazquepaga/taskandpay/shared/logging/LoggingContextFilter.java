package com.fazquepaga.taskandpay.shared.logging;

import com.fazquepaga.taskandpay.identity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoggingContextFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String USER_ID_KEY = "userId";
    private static final String REQUEST_URI_KEY = "requestUri";
    private static final String CLIENT_IP_KEY = "clientIp";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add correlationId to response headers
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        // Extract client IP address
        String clientIp = getClientIp(request);

        // Put initial contextual information into MDC
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(REQUEST_URI_KEY, request.getRequestURI());
        MDC.put(CLIENT_IP_KEY, clientIp);

        // Check if there is an authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof User user) {
            MDC.put(USER_ID_KEY, user.getId());
        } else {
            MDC.put(USER_ID_KEY, "anonymous");
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
