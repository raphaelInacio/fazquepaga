---
status: completed
---

# Task 3.0: Backend: Subscription Cancellation Logic & API Endpoint

## Overview

Tie the domain models and external services together into the core subscription service logic, expose it via a REST endpoint, and handle incoming Asaas webhooks for final tier downgrade.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- Create `cancelSubscription` method in `SubscriptionService` coordinating Asaas, Firestore update, and Notifications.
- Create `POST /api/v1/subscription/cancel` endpoint in `SubscriptionController`.
- Update `AsaasWebhookController` to handle `SUBSCRIPTION_DELETED` events to fully downgrade user from Premium.

## Subtasks

- [ ] 3.1 Implement `cancelSubscription(String userId, CancelSubscriptionRequest request)` in `SubscriptionService.java`.
- [ ] 3.2 Implement `POST /cancel` in `SubscriptionController.java` with proper authorization (PARENT role).
- [ ] 3.3 Update `AsaasWebhookController.java` to handle `SUBSCRIPTION_DELETED` event and trigger `confirmCancellation`.
- [ ] 3.4 Implement unit tests for `SubscriptionService` testing happy path and failure scenarios.
- [ ] 3.5 Implement integration tests for the `POST /cancel` endpoint.

## Implementation Details

From the tech spec:
- The user's local status becomes `PENDING_CANCELLATION`, but they retain `PREMIUM` tier until the Asaas webhook arrives or the paid period ends.
- Return `CancelSubscriptionResponse` indicating success and the expiration date.

### Relevant Files

- `src/main/java/.../subscription/SubscriptionService.java`
- `src/main/java/.../subscription/SubscriptionController.java`
- `src/main/java/.../payment/AsaasWebhookController.java`
- `src/test/java/.../subscription/SubscriptionServiceTest.java`

## Success Criteria

- Endpoint successfully processes cancellation requests.
- Firestore user document is updated with `PENDING_CANCELLATION`, reason, and timestamp.
- Asaas webhook properly finalizes cancellation.
- All unit and integration tests pass.
