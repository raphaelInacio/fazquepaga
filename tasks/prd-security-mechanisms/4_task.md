---
status: pending
---

# Task 4.0: reCAPTCHA v3 Integration

## Overview

Integrate Google reCAPTCHA v3 into authentication endpoints to detect and block bot traffic. reCAPTCHA v3 runs invisibly in the background, scoring requests based on user behavior without requiring user interaction (no puzzles).

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- reCAPTCHA v3 validation on auth endpoints (login, register, child login)
- Configurable score threshold (default: 0.5)
- Backend verification of reCAPTCHA tokens
- Frontend integration with React
- Logging of reCAPTCHA scores for analysis

## Subtasks

### Backend

- [ ] 4.1 Create `RecaptchaService` interface and implementation
- [ ] 4.2 Add reCAPTCHA configuration to `application.yml` (site-key, secret-key, threshold)
- [ ] 4.3 Implement token verification against Google's API
- [ ] 4.4 Add reCAPTCHA validation to `AuthController.login()` endpoint
- [ ] 4.5 Add reCAPTCHA validation to `AuthController.register()` endpoint
- [ ] 4.6 Add reCAPTCHA validation to `ChildController.login()` endpoint
- [ ] 4.7 Add environment variables for reCAPTCHA keys (`RECAPTCHA_SITE_KEY`, `RECAPTCHA_SECRET_KEY`)
- [ ] 4.8 Add structured logging for reCAPTCHA scores and failures
- [ ] 4.9 Write unit tests for `RecaptchaService`

### Frontend

- [ ] 4.10 Install `react-google-recaptcha-v3` package
- [ ] 4.11 Wrap app with `GoogleReCaptchaProvider`
- [ ] 4.12 Add reCAPTCHA token generation to login form
- [ ] 4.13 Add reCAPTCHA token generation to registration form
- [ ] 4.14 Add reCAPTCHA token generation to child login form
- [ ] 4.15 Add i18n messages for reCAPTCHA errors

### Setup

- [ ] 4.16 Register domain in [Google reCAPTCHA Admin Console](https://www.google.com/recaptcha/admin)
- [ ] 4.17 Document the setup process in project README or docs

## Implementation Details

### From Tech Spec - Interface

```java
public interface RecaptchaService {
    boolean verify(String token, String action);
    float getScore(String token);
}
```

### Configuration

```yaml
# application.yml
recaptcha:
  site-key: ${RECAPTCHA_SITE_KEY}
  secret-key: ${RECAPTCHA_SECRET_KEY}
  threshold: 0.5
  verify-url: https://www.google.com/recaptcha/api/siteverify
```

### Frontend Integration

```typescript
// React hook for reCAPTCHA v3
import { useGoogleReCaptcha } from 'react-google-recaptcha-v3';

const { executeRecaptcha } = useGoogleReCaptcha();

const handleLogin = async (credentials) => {
  const recaptchaToken = await executeRecaptcha('login');
  await api.login({ ...credentials, recaptchaToken });
};
```

### Relevant Files

- `backend/src/main/java/com/fazquepaga/taskandpay/security/RecaptchaService.java` [NEW]
- `backend/src/main/java/com/fazquepaga/taskandpay/controller/AuthController.java` [MODIFY]
- `backend/src/main/java/com/fazquepaga/taskandpay/controller/ChildController.java` [MODIFY]
- `backend/src/main/resources/application.yml` [MODIFY]
- `frontend/src/App.tsx` [MODIFY]
- `frontend/src/components/auth/LoginForm.tsx` [MODIFY]
- `frontend/src/components/auth/RegisterForm.tsx` [MODIFY]
- `frontend/src/components/child/ChildLoginForm.tsx` [MODIFY]
- `frontend/package.json` [MODIFY]

### reCAPTCHA Score Interpretation

| Score | Interpretation | Action |
|-------|----------------|--------|
| 0.0 - 0.3 | Likely bot | Block request |
| 0.3 - 0.5 | Suspicious | Block request (default threshold) |
| 0.5 - 0.7 | Uncertain | Allow (monitor) |
| 0.7 - 1.0 | Likely human | Allow |

## Success Criteria

- [ ] reCAPTCHA v3 is active on all auth endpoints
- [ ] Requests with score below threshold are rejected
- [ ] reCAPTCHA widget is invisible to users
- [ ] Scores are logged for pattern analysis
- [ ] Threshold is configurable via environment
- [ ] All tests pass
- [ ] Code is reviewed and approved
