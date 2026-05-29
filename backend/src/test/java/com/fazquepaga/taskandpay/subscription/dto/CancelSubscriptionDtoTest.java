package com.fazquepaga.taskandpay.subscription.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fazquepaga.taskandpay.identity.User.SubscriptionStatus;
import com.fazquepaga.taskandpay.subscription.CancellationReason;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CancelSubscriptionDtoTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        CancelSubscriptionRequest request =
                CancelSubscriptionRequest.builder()
                        .reason(CancellationReason.TOO_EXPENSIVE)
                        .reasonDetails("I cannot afford it right now")
                        .build();

        Set<ConstraintViolation<CancelSubscriptionRequest>> violations =
                validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenReasonIsNull_thenViolation() {
        CancelSubscriptionRequest request =
                CancelSubscriptionRequest.builder().reasonDetails("No reason given").build();

        Set<ConstraintViolation<CancelSubscriptionRequest>> violations =
                validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Cancellation reason is required");
    }

    @Test
    void whenReasonDetailsTooLong_thenViolation() {
        String longDetails = "a".repeat(501);
        CancelSubscriptionRequest request =
                CancelSubscriptionRequest.builder()
                        .reason(CancellationReason.OTHER)
                        .reasonDetails(longDetails)
                        .build();

        Set<ConstraintViolation<CancelSubscriptionRequest>> violations =
                validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Reason details cannot exceed 500 characters");
    }

    @Test
    void testRequestSerialization() throws Exception {
        CancelSubscriptionRequest request =
                CancelSubscriptionRequest.builder()
                        .reason(CancellationReason.TOO_EXPENSIVE)
                        .reasonDetails("Details")
                        .build();

        String json = objectMapper.writeValueAsString(request);
        assertThat(json).contains("\"reason\":\"TOO_EXPENSIVE\"");
        assertThat(json).contains("\"reasonDetails\":\"Details\"");

        CancelSubscriptionRequest deserialized =
                objectMapper.readValue(json, CancelSubscriptionRequest.class);
        assertThat(deserialized.getReason()).isEqualTo(CancellationReason.TOO_EXPENSIVE);
        assertThat(deserialized.getReasonDetails()).isEqualTo("Details");
    }

    @Test
    void testResponseSerialization() throws Exception {
        Instant now = Instant.now();
        CancelSubscriptionResponse response =
                CancelSubscriptionResponse.builder()
                        .status(SubscriptionStatus.PENDING_CANCELLATION)
                        .cancellationDate(now)
                        .message("Subscription cancelled successfully")
                        .build();

        String json = objectMapper.writeValueAsString(response);
        assertThat(json).contains("\"status\":\"PENDING_CANCELLATION\"");
        assertThat(json).contains("\"message\":\"Subscription cancelled successfully\"");

        CancelSubscriptionResponse deserialized =
                objectMapper.readValue(json, CancelSubscriptionResponse.class);
        assertThat(deserialized.getStatus()).isEqualTo(SubscriptionStatus.PENDING_CANCELLATION);
        assertThat(deserialized.getCancellationDate()).isEqualTo(now);
        assertThat(deserialized.getMessage()).isEqualTo("Subscription cancelled successfully");
    }
}
