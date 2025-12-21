---
status: pending
---

# Task 2.0: Subscription Flow & Webhooks

## Overview

Implement the Premium subscription flow using Asaas Checkout Session. This covers the API to generate the checkout link and the Webhook listener to activate the subscription upon payment.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.
 - Use `resources/asaas_integration_guide.md` for Asaas integration details.
 - Use asaas mcp para tirar duvidas sobre a plataforma
## Requirements

- Endpoint `POST /api/v1/subscription/subscribe`: Generate Checkout URL.
- Endpoint `POST /api/v1/webhooks/asaas`: Receive payment notifications.
- Security: Verify webhook requests (if possible) or ensure idempotency.
- Business Logic: Update `User.subscriptionStatus` and `subscriptionTier`.

## Subtasks

- [ ] 2.1 Implement `AsaasService.createCheckoutSession`.
- [ ] 2.2 Create `SubscriptionController` (`subscribe`, `status`).
- [ ] 2.3 Create `AsaasWebhookController` and handle `PAYMENT_RECEIVED`.
- [ ] 2.4 Implement `SubscriptionService` logic to upgrade user tier.
- [ ] 2.5 Unit Tests for Webhook processing (mocking payloads).

## Implementation Details

Use "Zero Data" approach - Redirect user to `checkoutUrl`.
Webhook payload examples in `docs/asaas_integration_guide.md`.

### Relevant Files

- `src/main/java/.../payment/AsaasWebhookController.java`
- `src/main/java/.../subscription/SubscriptionService.java`

## Success Criteria

- Calling subscribe API returns a valid Asaas URL.
- Sending a mock `PAYMENT_RECEIVED` webhook updates the user to `PREMIUM`.
