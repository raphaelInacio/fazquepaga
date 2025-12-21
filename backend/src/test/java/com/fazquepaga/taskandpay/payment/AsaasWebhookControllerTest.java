package com.fazquepaga.taskandpay.payment;

import com.fazquepaga.taskandpay.payment.dto.AsaasWebhookEvent;
import com.fazquepaga.taskandpay.subscription.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AsaasWebhookControllerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private AsaasWebhookController webhookController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleWebhook_PaymentReceived_ShouldActivateSubscription() {
        // Arrange
        AsaasWebhookEvent event = new AsaasWebhookEvent();
        event.setEvent("PAYMENT_RECEIVED");

        AsaasWebhookEvent.PaymentInfo payment = new AsaasWebhookEvent.PaymentInfo();
        payment.setExternalReference("user123");
        payment.setSubscription("sub_456");
        event.setPayment(payment);

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(event);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        verify(subscriptionService).activateSubscription("user123", "sub_456");
    }

    @Test
    void handleWebhook_OtherEvent_ShouldIgnore() {
        // Arrange
        AsaasWebhookEvent event = new AsaasWebhookEvent();
        event.setEvent("PAYMENT_OVERDUE");

        // Act
        ResponseEntity<Void> response = webhookController.handleWebhook(event);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        verify(subscriptionService, never()).activateSubscription(anyString(), anyString());
    }
}
