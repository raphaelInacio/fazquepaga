---
status: completed
---

# Task 7.0: Frontend: Allowance Withdrawal

## Overview

Implement individual interfaces for Child (Request Withdrawal) and Parent (Approve Withdrawal/Mark as Paid).

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

### Child Portal
- **Withdrawal Request**: UI to input amount and request withdrawal.
- **Validation**: Cannot request more than current balance.
- **History**: View status of past requests (Pending, Paid).

### Parent Dashboard
- **Notification/List**: View pending withdrawal requests from children.
- **Action**: Button to "Mark as Paid" (Manual off-platform payment confirmation).
- **Integration**: Call `POST /api/v1/children/{childId}/withdraw` and `POST /api/v1/withdrawals/{id}/approve`.

## Subtasks

- [ ] 7.1 **Child**: Add "Withdraw" button/modal in the Balance section.
- [ ] 7.2 **Child**: Connect to `POST /withdraw` API.
- [ ] 7.3 **Parent**: Add "Withdrawal Requests" section in the Ledger/Financial view.
- [ ] 7.4 **Parent**: Connect to `POST /approve` API.
- [ ] 7.5 **Tests**: Add Unit/Integration tests for the Withdrawal Form checks (balance validation).

## Implementation Details

- Reuse existing Ledger components if possible.
- Ensure clear feedback (Toasts/Alerts) when actions succeed.

## Success Criteria

- Child can successfully request a valid amount.
- Parent sees the request immediately.
- Parent can mark it as paid, updating the status in the UI.
