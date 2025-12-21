package com.fazquepaga.taskandpay.payment;

import com.fazquepaga.taskandpay.payment.dto.AsaasWebhookEvent;
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

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody AsaasWebhookEvent event) {
        log.info("Received Asaas Webhook: {}", event.getEvent());

        if ("PAYMENT_RECEIVED".equals(event.getEvent()) || "SUBSCRIPTION_CREATED".equals(event.getEvent())) {
            // "SUBSCRIPTION_CREATED" might be too early if payment not confirmed,
            // but PAYMENT_RECEIVED is definitive for activation.
            // Let's stick to PAYMENT_RECEIVED for activation logic to be safe.
            // Or maybe handle both if we want to store subscriptionId early.

            if ("PAYMENT_RECEIVED".equals(event.getEvent())) {
                String userId = event.getPayment().getExternalReference();
                String subscriptionId = event.getPayment().getSubscription();

                if (userId != null) {
                    subscriptionService.activateSubscription(userId, subscriptionId);
                } else {
                    log.warn("Webhook received without externalReference: {}", event);
                }
            }
        }

        return ResponseEntity.ok().build();
    }
}
