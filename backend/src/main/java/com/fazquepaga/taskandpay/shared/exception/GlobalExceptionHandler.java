package com.fazquepaga.taskandpay.shared.exception;

import com.fazquepaga.taskandpay.ai.AIQuotaExceededException;
import com.fazquepaga.taskandpay.subscription.SubscriptionLimitReachedException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@lombok.extern.slf4j.Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler({
            ExecutionException.class,
            InterruptedException.class,
            RuntimeException.class
    })
    public ResponseEntity<ApiError> handleInternalServerErrors(
            Exception ex, HttpServletRequest request) {
        log.error("Uncaught exception: ", ex);
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("error.internal", null, locale);
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, message, request.getRequestURI());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Bad Request: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SubscriptionLimitReachedException.class)
    public ResponseEntity<ApiError> handleSubscriptionLimitReachedException(
            SubscriptionLimitReachedException ex, HttpServletRequest request) {
        log.warn("Subscription limit reached: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.PAYMENT_REQUIRED, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(apiError, HttpStatus.PAYMENT_REQUIRED);
    }

    @ExceptionHandler(AIQuotaExceededException.class)
    public ResponseEntity<ApiError> handleAIQuotaExceededException(
            AIQuotaExceededException ex, HttpServletRequest request) {
        log.warn("AI quota exceeded: {} remaining={} limit={}",
                ex.getMessage(), ex.getRemainingQuota(), ex.getDailyLimit());
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("error.ai_quota_exceeded", null, locale);
        ApiError apiError = new ApiError(HttpStatus.TOO_MANY_REQUESTS, message, request.getRequestURI());
        return new ResponseEntity<>(apiError, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(com.fazquepaga.taskandpay.security.RecaptchaException.class)
    public ResponseEntity<ApiError> handleRecaptchaException(
            com.fazquepaga.taskandpay.security.RecaptchaException ex, HttpServletRequest request) {
        log.warn("reCAPTCHA verification failed: {} path={}", ex.getMessage(), request.getRequestURI());
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("error.recaptcha_failed", null,
                "Security verification failed. Please try again.", locale);
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
}
