---
status: pending
---

# Task 5.0: Feature: AI Context & Prompt Refinement

## Overview

Enhance the AI task suggestion engine by incorporating a text-based context (Bio/Interests) about the child.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- Update `Child` entity with `aiContext` field.
- Endpoint to update this context.
- Update the Prompt sent to Vertex AI (Gemini) to include this context.

## Subtasks

- [ ] 5.1 Update `Child` entity (add `aiContext`).
- [ ] 5.2 Implement `PATCH /children/{id}/context` endpoint.
- [ ] 5.3 Update `AiTaskService` prompt template to inject the context.
- [ ] 5.4 Test if suggestions change based on context (Manual verification).

## Implementation Details

Prompt adjustment: "Consider updates: {context}" when asking for tasks.

### Relevant Files

- `src/main/java/.../domain/Child.java`
- `src/main/java/.../ai/AiTaskService.java`

## Success Criteria

- Can save `aiContext` for a child.
- AI Suggestions reflect the provided context (e.g., "likes dinosaurs" -> "Organize dino toys").
