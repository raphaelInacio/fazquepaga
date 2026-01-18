# Plan for PRD Creation: 100 Deposits Challenge

## 1. Goal
Create a comprehensive PRD for the "Desafio 100 Depósitos" feature, integrated into the TaskAndPay platform.

## 2. Understanding (Based on Image & Context)
- **Concept**: A gamified savings challenge where the user fills 100 slots (likely numbered 1-100).
- **Total Value**: R$ 5,050.00 (Sum of 1 to 100).
- **Visual**: A grid 10x10.
- **Context**: TaskAndPay is a chore/allowance app. This feature likely encourages saving.

## 3. Clarifying Questions (REQUIRED)
Before drafting the full PRD, I need to clarify the following mechanics:

1.  **Target Audience & Interaction**:
    -   Is this primarily for the **Child** to interact with (marking deposits)?
    -   Or for the **Parent** to manage?
2.  **Money Flow**:
    -   **Virtual vs Real**: Is this just a tracker for physical cash (piggy bank)?
    -   **In-App Transfer**: Or does marking a number *deduct* from the child's "Available Balance" (from completed chores) and move it to a "Savings/Locked" status?
3.  **Flexibility**:
    -   Must they do it in order (1, 2, 3...)? Or random (pick "50" today)?
    -   Can custom amounts be set, or is it strictly 1-100?
4.  **Completion**:
    -   What happens when finished? (Notification to parent? Unlock a configured reward?)

## 4. Proposed PRD Outline
I will follow the standard `_prd-template.md` with specific focus on:

-   **User Stories**:
    -   "As a child, I want to mark a number so I can see my progress."
    -   "As a parent, I want to see the total saved in the challenge."
-   **Core Features**:
    -   Interactive Grid (1-100).
    -   Progress tracking (Total saved, % complete).
    -   Balance movement logic (if applicable).
-   **UX/UI**:
    -   Gamification elements (confetti, badges).
    -   Based on the provided image style.

## 5. Next Steps
1.  **Approve/Answer**: Please answer the clarifying questions and approve this plan.
2.  **Draft**: I will draft the `_prd.md` following the template.
3.  **Review**: Final review of the generated PRD.
