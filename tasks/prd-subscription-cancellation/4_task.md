---
status: done
---

# Task 4.0: Frontend: Cancellation Flow UI & Integration

## Overview

Implement the self-service cancellation user interface, including the churn survey modal and the impact confirmation screen.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- Add a "Cancel Subscription" button on the Settings page for active Premium users.
- Implement a modal flow: Step 1 (Survey) -> Step 2 (Impact Warning) -> API Call -> Success/Error.
- Connect the UI to the backend `POST /cancel` endpoint.

## Subtasks

- [ ] 4.1 Update `subscriptionService.ts` with the `cancelSubscription` API call.
- [ ] 4.2 Create `CancelSubscriptionModal.tsx` component handling the multi-step flow.
- [ ] 4.3 Update `Settings.tsx` to display the cancel button (only for active premium users) and trigger the modal.
- [ ] 4.4 Add necessary i18n translation keys in `pt.json` and `en.json`.
- [ ] 4.5 Add component and integration tests (Playwright/Vitest as per frontend rules).

## Implementation Details

From the tech spec:
- The button should have a destructive visual style (red).
- Step 1: Radio buttons for predefined reasons + "Other" text input. Reason is mandatory.
- Step 2: Warning UI (yellow/orange) listing impacts (child limit, AI loss) and showing expiration date.
- Micro-copy should be empathetic.

### Relevant Files

- `frontend/src/services/subscriptionService.ts`
- `frontend/src/components/CancelSubscriptionModal.tsx` (New)
- `frontend/src/pages/Settings.tsx`
- `frontend/src/locales/pt.json` & `en.json`

## Success Criteria

- Premium users can successfully cancel their subscription from the UI.
- Appropriate feedback (toasts) is displayed on success or error.
- The UI handles loading states correctly during API calls.
- Automated tests pass.
