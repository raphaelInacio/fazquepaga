---
status: complete
---

# Task 1.0: Backend: Core Domain & Data Transfer Objects

## Overview

Update the core domain models and create necessary Data Transfer Objects (DTOs) to support the subscription cancellation flow.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- Add `PENDING_CANCELLATION` to `SubscriptionStatus` enum in `User.java`.
- Add cancellation tracking fields to `User.java` (`cancellationDate`, `cancellationReason`, `cancellationReasonDetails`).
- Create `CancellationReason` enum.
- Create request and response DTOs for the cancellation endpoint.

## Subtasks

- [ ] 1.1 Create `CancellationReason` enum in `subscription/CancellationReason.java`.
- [ ] 1.2 Update `User.java` to include `PENDING_CANCELLATION` status and new fields.
- [ ] 1.3 Create `CancelSubscriptionRequest` DTO.
- [ ] 1.4 Create `CancelSubscriptionResponse` DTO.
- [ ] 1.5 Implement unit tests to ensure DTO serialization/deserialization and enum mapping work correctly.

## Implementation Details

From the tech spec:
- `CancellationReason` enum values: `TOO_EXPENSIVE`, `NOT_USING_FEATURES`, `FOUND_ALTERNATIVE`, `WILL_RETURN_LATER`, `OTHER`.
- New fields in `User.java`:
  - `Instant cancellationDate;`
  - `CancellationReason cancellationReason;`
  - `String cancellationReasonDetails;`

### Relevant Files

- `src/main/java/.../identity/User.java`
- `src/main/java/.../subscription/CancellationReason.java`
- `src/main/java/.../subscription/dto/CancelSubscriptionRequest.java`
- `src/main/java/.../subscription/dto/CancelSubscriptionResponse.java`

## Success Criteria

- Domain model successfully updated and compiles.
- DTOs correctly map to JSON payloads.
- Unit tests pass.
