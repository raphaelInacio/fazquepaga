package com.fazquepaga.taskandpay.shared.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiErrorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateApiErrorWithAllFields() {
        // Given
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Invalid input";
        String path = "/api/v1/test";

        // When
        ApiError apiError = new ApiError(status, message, path);

        // Then
        assertNotNull(apiError);
        assertEquals(400, apiError.getStatus());
        assertEquals("Bad Request", apiError.getError());
        assertEquals(message, apiError.getMessage());
        assertEquals(path, apiError.getPath());
        assertNotNull(apiError.getTimestamp());
    }

    @Test
    void shouldCreateApiErrorForInternalServerError() {
        // Given
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "An error occurred";
        String path = "/api/v1/tasks";

        // When
        ApiError apiError = new ApiError(status, message, path);

        // Then
        assertEquals(500, apiError.getStatus());
        assertEquals("Internal Server Error", apiError.getError());
        assertEquals(message, apiError.getMessage());
        assertEquals(path, apiError.getPath());
    }

    @Test
    void shouldCreateApiErrorForNotFound() {
        // Given
        HttpStatus status = HttpStatus.NOT_FOUND;
        String message = "Resource not found";
        String path = "/api/v1/users/123";

        // When
        ApiError apiError = new ApiError(status, message, path);

        // Then
        assertEquals(404, apiError.getStatus());
        assertEquals("Not Found", apiError.getError());
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        // Given
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Test message", "/api/v1/test");

        // When
        String json = objectMapper.writeValueAsString(apiError);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"status\":400"));
        assertTrue(json.contains("\"error\":\"Bad Request\""));
        assertTrue(json.contains("\"message\":\"Test message\""));
        assertTrue(json.contains("\"path\":\"/api/v1/test\""));
        assertTrue(json.contains("\"timestamp\""));
    }

    @Test
    void shouldSetTimestampAutomatically() throws InterruptedException {
        // Given
        java.time.LocalDateTime before = java.time.LocalDateTime.now();

        // Small delay to ensure timestamp is set
        Thread.sleep(10);

        // When
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Test", "/test");

        // Then
        assertNotNull(apiError.getTimestamp());
        assertTrue(
                apiError.getTimestamp().isAfter(before)
                        || apiError.getTimestamp().isEqual(before));
    }
}
