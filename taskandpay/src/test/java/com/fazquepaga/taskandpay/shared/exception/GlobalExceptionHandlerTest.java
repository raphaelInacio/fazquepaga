package com.fazquepaga.taskandpay.shared.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private org.springframework.context.MessageSource messageSource;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock MessageSource to return a default error message
        when(messageSource.getMessage(any(String.class), any(), any(java.util.Locale.class)))
                .thenReturn("An internal error occurred. Please try again later.");
    }

    @Test
    void shouldHandleExecutionException() {
        // Given
        ExecutionException exception = new ExecutionException("Test error", new Throwable());
        when(request.getRequestURI()).thenReturn("/api/v1/test");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleInternalServerErrors(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals(
                "An internal error occurred. Please try again later.",
                response.getBody().getMessage());
        assertEquals("/api/v1/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldHandleInterruptedException() {
        // Given
        InterruptedException exception = new InterruptedException("Thread interrupted");
        when(request.getRequestURI()).thenReturn("/api/v1/tasks");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleInternalServerErrors(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals(
                "An internal error occurred. Please try again later.",
                response.getBody().getMessage());
        assertEquals("/api/v1/tasks", response.getBody().getPath());
    }

    @Test
    void shouldHandleRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected runtime error");
        when(request.getRequestURI()).thenReturn("/api/v1/children");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleInternalServerErrors(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid parameter");
        when(request.getRequestURI()).thenReturn("/api/v1/auth/register");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleIllegalArgumentException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Invalid parameter", response.getBody().getMessage());
        assertEquals("/api/v1/auth/register", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldHandleIllegalArgumentExceptionWithCustomMessage() {
        // Given
        String customMessage = "Parent with ID parent-123 not found.";
        IllegalArgumentException exception = new IllegalArgumentException(customMessage);
        when(request.getRequestURI()).thenReturn("/api/v1/children");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleIllegalArgumentException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(customMessage, response.getBody().getMessage());
    }

    @Test
    void shouldIncludeTimestampInErrorResponse() {
        // Given
        RuntimeException exception = new RuntimeException("Test");
        when(request.getRequestURI()).thenReturn("/test");

        // When
        ResponseEntity<ApiError> response = exceptionHandler.handleInternalServerErrors(exception, request);

        // Then
        assertNotNull(response.getBody().getTimestamp());
        // Verify timestamp is recent (within last minute)
        assertTrue(
                response.getBody()
                        .getTimestamp()
                        .isAfter(java.time.LocalDateTime.now().minusMinutes(1)));
    }
}
