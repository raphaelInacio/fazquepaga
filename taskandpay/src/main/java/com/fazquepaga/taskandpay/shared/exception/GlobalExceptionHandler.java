package com.fazquepaga.taskandpay.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@lombok.extern.slf4j.Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({
        ExecutionException.class,
        InterruptedException.class,
        RuntimeException.class
    })
    public ResponseEntity<ApiError> handleInternalServerErrors(
            Exception ex, HttpServletRequest request) {
        log.error("Uncaught exception: ", ex);
        ApiError apiError =
                new ApiError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An internal error occurred. Please try again later.",
                        request.getRequestURI());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Bad Request: {}", ex.getMessage());
        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
}
