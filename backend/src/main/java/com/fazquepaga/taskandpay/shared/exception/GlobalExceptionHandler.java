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

    /**
     * Create a GlobalExceptionHandler that uses the provided MessageSource to resolve localized error messages.
     *
     * @param messageSource the MessageSource used to look up localized messages for exception responses
     */
    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Handles unexpected server-side exceptions and maps them to a standardized 500 error response.
     *
     * @param ex the exception that was thrown
     * @param request the HTTP request whose URI is included in the error payload
     * @return a ResponseEntity containing an ApiError with HTTP 500 and a localized internal error message
     */
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

    /**
     * Handle IllegalArgumentException by logging a warning and producing a 400 Bad Request ApiError.
     *
     * @param ex the thrown IllegalArgumentException
     * @param request the servlet request whose URI will be included in the error
     * @return a ResponseEntity containing an ApiError with HTTP 400, the exception message, and the request URI
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Bad Request: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles SubscriptionLimitReachedException and returns a 402 Payment Required response with an ApiError containing the exception message and the request URI.
     *
     * @param ex the SubscriptionLimitReachedException that triggered this handler
     * @param request the HTTP servlet request whose URI is included in the ApiError
     * @return a ResponseEntity containing an ApiError with status 402 Payment Required, the exception message, and the request URI
     */
    @ExceptionHandler(SubscriptionLimitReachedException.class)
    public ResponseEntity<ApiError> handleSubscriptionLimitReachedException(
            SubscriptionLimitReachedException ex, HttpServletRequest request) {
        log.warn("Subscription limit reached: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.PAYMENT_REQUIRED, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(apiError, HttpStatus.PAYMENT_REQUIRED);
    }

    /**
     * Handle an AIQuotaExceededException by returning a 429 response with a localized quota-exceeded message.
     *
     * @param ex      the exception indicating the AI quota has been exceeded
     * @param request the HTTP request from which the request URI is taken for the error payload
     * @return        a ResponseEntity containing an ApiError with HTTP 429 (Too Many Requests) and a localized message
     */
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

    /**
     * Handle a failed reCAPTCHA verification and produce a Bad Request error response.
     *
     * Logs the verification failure, obtains a localized message for "error.recaptcha_failed"
     * (falls back to "Security verification failed. Please try again."), and returns an
     * ApiError containing the localized message and the request URI.
     *
     * @param ex the RecaptchaException containing verification failure details
     * @param request the HTTP servlet request whose URI will be included in the error payload
     * @return a ResponseEntity containing an ApiError with HTTP 400 (Bad Request)
     */
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