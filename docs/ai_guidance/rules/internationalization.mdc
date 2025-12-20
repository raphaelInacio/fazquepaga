---
alwaysApply: true
---

# Internationalization (i18n)

## Principles

- **No Hardcoded Strings**: All user-facing text must be retrieved via translation keys.
- **Files**:
  - `frontend/src/locales/en.json` (English)
  - `frontend/src/locales/pt.json` (Portuguese - Primary)

## Workflow

When adding new UI text:

1.  Define a detailed key (e.g., `dashboard.tasks.approve_button`).
2.  Add the key to **BOTH** `en.json` and `pt.json`.
3.  Use the `t()` function (from `react-i18next` or similar hook) in the component.

```tsx
// Bad
<span>Hello World</span>

// Good
<span>{t('welcome_message')}</span>
```

## Review

- Verify that keys follow the nested structure of the JSON files.
- Ensure no "missing key" warnings appear in the console during execution.
