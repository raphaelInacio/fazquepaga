---
status: complete
---

# Task 2.0: Backend: External Services (Asaas & Notification)

## Overview

Implement integration with external services required for cancellation: the Asaas API for terminating the subscription billing, and the Notification Service for sending WhatsApp confirmations.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- Update `AsaasService` to call `DELETE /v3/subscriptions/{id}`.
- Gracefully handle 404 errors from Asaas as successful cancellations (already deleted externally).
- Update `NotificationService` to support a new `SUBSCRIPTION_CANCELED` notification event.

## Subtasks

- [x] 2.1 Update `AsaasService.java` with `boolean cancelSubscription(String subscriptionId)`.
- [x] 2.2 Update `NotificationType` enum with `SUBSCRIPTION_CANCELED`.
- [x] 2.3 Implement `sendSubscriptionCanceled(User user, Instant premiumExpirationDate)` in `NotificationService.java`.
- [x] 2.4 Implement unit tests for `AsaasService` mocking the `RestTemplate` responses (success, 404, 500).
- [x] 2.5 Implement unit tests for `NotificationService`.

## Implementation Details

From the tech spec:
- API call: `DELETE /v3/subscriptions/{id}`
- Headers: `access_token: ${asaas.api-key}`
- Errors: `404` logged as warning but proceed. `400`/`500` throw exception to abort local status change.

### Relevant Files

- `src/main/java/.../payment/AsaasService.java`
- `src/main/java/.../notification/NotificationService.java`
- `src/main/java/.../notification/NotificationType.java`
- `src/test/java/.../payment/AsaasServiceTest.java`

## Success Criteria

- `AsaasService` successfully deletes subscriptions and handles API errors.
- `NotificationService` formats and publishes the correct event to Pub/Sub.
- All unit tests pass.
