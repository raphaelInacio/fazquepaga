---
status: pending
---

# Task 6.0: Frontend: Subscription Flow

## Overview

Implement the frontend user interface for the Premium Subscription flow. This includes the pricing/subscription page, handling the call to generate the checkout link, and redirecting the user to Asaas.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- **Pricing Page/Component**: Display "Free" vs "Premium" benefits.
- **Subscribe Action**: Button to "Upgrade to Premium".
- **Integration**: Call `POST /api/v1/subscription/subscribe` to get the checkout URL.
- **Redirection**: Redirect the browser to the returned Asaas URL.
- **Return Flow**: Handle the user returning (success/cancel pages if applicable) and updating the UI state (Subscription Status).
- **Dashboard Update**: Show "Premium Active" badge or similar in the Parent Dashboard.

## Subtasks

- [ ] 6.1 Create `SubscriptionPage` or `PricingModal` component.
- [ ] 6.2 Implement `SubscriptionService` (Frontend) to call the API.
- [ ] 6.3 Handle 'Upgrade' click -> API Call -> Window Redirect.
- [ ] 6.4 Update `UserContext` or `store` to reflect new subscription status (re-fetch user profile on return).
- [ ] 6.5 **Tests**: Add Component tests (React Testing Library) for the Pricing UI and Interaction.

## Implementation Details

- Use standard Button components from the design system.
- Ensure the API call is secure (auth headers).

## Success Criteria

- User can click "Upgrade" and is redirected to Asaas.
- Upon payment (and webhook processing), the user sees "Premium" status in the app (after refresh/re-login).
