---
alwaysApply: true
---

# Frontend Testing Strategy

## Core Principle
- **Prioritize E2E Testing**: We do not write Unit Tests for Frontend components (React).
- **Tooling**: Use the **Browser MCP** tool to perform End-to-End (E2E) testing.
- **Why?**: To reduce maintenance overhead of brittle unit tests and focus on user-centric validation.

## Directives
1.  **No Unit Tests**: Do not create `.test.tsx` or `.spec.tsx` files for React components unless explicitly requested for complex algorithmic logic (which should be extracted to pure TS/JS functions).
2.  **Browser Verification**: When verifying frontend tasks, use the `browser_subagent` or `read_browser_page` tools to navigate the actual running application (or a deployed URL).
3.  **Manual Validation**: If automated browser testing is complex, provide a detailed **Manual Verification Plan** for the user to execute.

## Exceptions
- Pure utility functions (e.g., helpers, formatting logic) may have unit tests if they contain significant complexity.
