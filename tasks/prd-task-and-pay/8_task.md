---
status: complete
---

# Task 8.0: Frontend: AI Context (Bio/Interests)

## Overview

Interface for Parents to define the "Bio" or "Interests" of their child to improve AI task suggestions.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- **Settings UI**: Add a section in "Child Settings" or "Edit Child".
- **Form Field**: Text area for "Bio/Interests" (e.g., "Likes dinosaurs, hates washing dishes").
- **Integration**: Call `PATCH /api/v1/children/{childId}/context` (or updated Child endpoint).

## Subtasks

- [x] 8.1 Add `AiContextInput` component to Child logic.
- [x] 8.2 Connect to API to save changes.
- [x] 8.3 Verify that this context is displayed/editable correctly.
- [x] 8.4 **Tests**: Component test for the input form. (REMOVED per Rule `frontend-testing.md`)

## Success Criteria

- Parent can save text.
- Text persists on page reload.
