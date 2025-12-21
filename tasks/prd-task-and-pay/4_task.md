---
status: pending
---

# Task 4.0: Feature: Allowance Withdrawal

## Overview

Implement the logic, data models, and APIs for children to request withdrawals and parents to approve them (marking as paid).

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- Update `Ledger` model (types, status).
- API for Child to request withdraw (`POST /withdraw`).
- API for Parent to approve withdraw (`POST /approve`).
- Trigger Notifications (Parent gets request, Child gets confirmation).
- Validation: Child must have sufficient balance.

## Subtasks

- [ ] 4.1 Update `Ledger` entity (add `status`, `type`).
- [ ] 4.2 Implement `WithdrawalService` (balance check, create transaction).
- [ ] 4.3 Implement APIs in `AllowanceController` (or new `WithdrawalController`).
- [ ] 4.4 Integrate with `NotificationService`.

## Implementation Details

Remember: No actual money movement logic here, just record keeping (Ledger).

### Relevant Files

- `src/main/java/.../allowance/Ledger.java`
- `src/main/java/.../allowance/WithdrawalService.java`

## Success Criteria

- Child can request withdrawal if balance > 0.
- Parent receives notification.
- Parent can mark as paid, deducting balance (visually) or closing the transaction.
