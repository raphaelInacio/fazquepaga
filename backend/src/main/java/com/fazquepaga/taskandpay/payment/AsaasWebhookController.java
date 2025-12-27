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

        String asaasCustomerId = null;
        String subscriptionId = null;
        String checkoutSessionId = null;

        if (event.getPayment() != null) {
            asaasCustomerId = event.getPayment().getCustomer();
            subscriptionId = event.getPayment().getSubscription();
            checkoutSessionId = event.getPayment().getCheckoutSession();
        }

        if (asaasCustomerId == null) {
            log.warn("Webhook event {} received without Customer ID.", eventType);
            return ResponseEntity.ok().build();
        }

        switch (eventType) {
            case PAYMENT_CONFIRMED:
            case PAYMENT_RECEIVED:
                subscriptionService.activateSubscription(asaasCustomerId, subscriptionId, checkoutSessionId);
                break;
            case PAYMENT_OVERDUE:
                subscriptionService.deactivateSubscription(
                        asaasCustomerId, com.fazquepaga.taskandpay.identity.User.SubscriptionStatus.PAST_DUE,
                        checkoutSessionId);
                break;
            case PAYMENT_REFUNDED:
            case CHARGEBACK_REQUESTED:
                subscriptionService.deactivateSubscription(
                        asaasCustomerId, com.fazquepaga.taskandpay.identity.User.SubscriptionStatus.CANCELED,
                        checkoutSessionId);
                break;
            default:
                log.debug("Ignored Asaas Webhook event type: {}", eventType);
        }

        return ResponseEntity.ok().build();
    }
}
