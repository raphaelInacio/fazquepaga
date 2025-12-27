package com.fazquepaga.taskandpay.payment;

import com.fazquepaga.taskandpay.payment.dto.AsaasWebhookEvent;
import com.fazquepaga.taskandpay.payment.dto.AsaasWebhookEventType;
import com.fazquepaga.taskandpay.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks/asaas")
@RequiredArgsConstructor
@Slf4j
public class AsaasWebhookController {

    private final SubscriptionService subscriptionService;

    @org.springframework.beans.factory.annotation.Value("${asaas.webhook.accessToken}")
    private String webhookAccessToken;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody AsaasWebhookEvent event,
            @RequestHeader(value = "asaas-access-token", required = false) String accessToken) {
        log.info("Received Asaas Webhook: {}", event.getEvent());

        if (accessToken == null || !accessToken.equals(webhookAccessToken)) {
            log.warn("Webhook received with invalid or missing token. Header: {}", accessToken);
            return ResponseEntity.status(403).build();
        }

        AsaasWebhookEventType eventType = AsaasWebhookEventType.fromString(event.getEvent());
        
        // If event is irrelevant or unknown, just log debug and 200 OK
        if (eventType == AsaasWebhookEventType.UNKNOWN) {
             log.debug("Ignored or Unknown Asaas Webhook event: {}", event.getEvent());
             return ResponseEntity.ok().build();
        }

        String userId = null;
        String subscriptionId = null;
            // 

            // 
        if (event.getPayment() != null) {
            userId = event.getPayment().getExternalReference();
            subscriptionId = event.getPayment().getSubscription();
        }

        if (userId == null) {
            log.warn("Webhook event {} received without valid externalReference/userId", eventType);
            return ResponseEntity.ok().build();
        }

        switch (eventType) {
            case PAYMENT_CONFIRMED:
            case PAYMENT_RECEIVED:
                subscriptionService.activateSubscription(userId, subscriptionId);
                break;
            case PAYMENT_OVERDUE:
                subscriptionService.deactivateSubscription(
                        userId, com.fazquepaga.taskandpay.identity.User.SubscriptionStatus.PAST_DUE);
                break;
            case PAYMENT_REFUNDED:
            case CHARGEBACK_REQUESTED:
                subscriptionService.deactivateSubscription(
                        userId, com.fazquepaga.taskandpay.identity.User.SubscriptionStatus.CANCELED);
                break;
            default:
                log.debug("Ignored Asaas Webhook event type: {}", eventType);
        }

        return ResponseEntity.ok().build();
    }
}
