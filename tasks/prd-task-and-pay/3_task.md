---
status: pending
---

# Task 3.0: Logic: Notification Hub & WhatsApp Templates

## Overview

Centralize notification logic. Instead of scattered Twilio calls, create a `NotificationService` that handles different events (Task Done, Withdrawal Requested) and formats messages appropriately for WhatsApp.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- Create `notification` package.
- Define events: `TaskCompleted`, `WithdrawalRequested`, `WithdrawalPaid`.
- **Async Architecture**: Use Google Cloud Pub/Sub to decouple event producers from the notification sender.
- Implement message templates ("João completou a tarefa...", "Seu saque foi pago!").
- Integrate with existing `whatsapp` module to send messages.

## Subtasks

- [ ] 3.1 Create `NotificationService` (Producer) that publishes events to Pub/Sub.
- [ ] 3.2 Create `NotificationListener` (Consumer) that listens to Pub/Sub and calls `WhatsappClient`.
- [ ] 3.3 Move existing task notification logic to this async flow.
- [ ] 3.4 Create templates for new events (Withdrawal).
- [ ] 3.5 Verify WhatsApp delivery via Pub/Sub emulator/dev environment.

## Implementation Details

Use the existing `whatsapp` module as the transport layer.

### Relevant Files

### Relevant Files

- `src/main/java/.../notification/NotificationService.java` (Publisher)
- `src/main/java/.../notification/NotificationListener.java` (Subscriber)
- `src/main/java/.../whatsapp/WhatsappClient.java`

## Success Criteria

- All system notifications are sent via `NotificationService`.
- New templates are correctly formatted and delivered.
