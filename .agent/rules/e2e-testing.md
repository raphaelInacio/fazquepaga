---
trigger: model_decision
---

# E2E Testing & Frontend Validation

## Frontend validation

- **MCP Browser Tool**: Any significant frontend change MUST be validated using the `browser_action` tool to render the page and verify visually (taking screenshots or checking DOM status).
- **Do Not Guess**: Do not assume CSS fits perfectly without checking.

## Playwright Tests

- **Location**: `frontend/e2e` (or similar root directory).
- **Update Rule**: If you change a UI component (ID, class, text flow), you MUST run the associated E2E test.
- **Failures**: If a test (like `child-portal.spec.ts`) fails:
    1.  Analyze the trace/log.
    2.  Determine if it's a regression or a test drift.
    3.  Fix the code or update the test selectors.

## Selectors

- **Test IDs**: Prefer `data-testid` attributes for selecting elements in tests.
  - `<button data-testid="submit-task">`
- **Avoid**: Extremely coupled XPath or CSS selectors (e.g., `div > div > span:nth-child(3)`).

## Mocking

- Use Playwright's network interception to mock backend calls (`page.route`) to ensure deterministic UI tests.
- **Asaas Integration Mocks**: When writing tests for flows involving Asaas, ensure you intercept the API calls to avoid hitting external services during local E2E runs.
  ```typescript
  await page.route('**/v3/payments', async (route) => {
    const request = route.request();
    if (request.method() === 'POST') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: "pay_123456",
          invoiceUrl: "https://sandbox.asaas.com/i/123456",
          status: "PENDING"
        })
      });
    } else {
      await route.continue();
    }
  });
  ```