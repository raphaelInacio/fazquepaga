---
status: done
---

# Task 3.0: AI Quota Service Implementation

## Overview

Implement a quota system to control and limit usage of AI-powered endpoints. This protects against unexpected costs from AI API calls and provides a differentiated experience between Free and Premium users.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- Daily quota limits per user (Free: 5/day, Premium: 10/day)
- Automatic quota reset at midnight (user's timezone)
- Friendly error message when quota is exceeded
- Quota tracking stored in Firestore
- Logging of AI usage for analysis

## Subtasks

- [ ] 3.1 Create `AIQuota` model class for Firestore
- [ ] 3.2 Create `AIQuotaRepository` for Firestore operations
- [ ] 3.3 Create `AIQuotaService` interface and implementation
- [ ] 3.4 Implement daily quota check logic (`canUseAI`)
- [ ] 3.5 Implement usage recording (`recordUsage`)
- [ ] 3.6 Implement quota reset logic (check `lastResetDate` vs current date)
- [ ] 3.7 Add quota verification to `TaskController.suggestTasks()` endpoint
- [ ] 3.8 Add quota verification to image validation endpoint (if exists)
- [ ] 3.9 Create i18n messages for quota exceeded (`error.ai_quota_exceeded`)
- [ ] 3.10 Add structured logging for quota events
- [ ] 3.11 Write unit tests for `AIQuotaService`
- [ ] 3.12 Write integration tests for quota-protected endpoints

## Implementation Details

### From Tech Spec - Data Model

```java
@Data
public class AIQuota {
    private String userId;
    private int usedToday;           // Count for today
    private LocalDate lastResetDate; // Last reset date
    private int dailyLimit;          // 5 (Free) or 10 (Premium)
}
```

**Firestore Path**: `users/{userId}/quotas/ai`

### From Tech Spec - Interface

```java
public interface AIQuotaService {
    boolean canUseAI(String userId);
    void recordUsage(String userId);
    int getRemainingQuota(String userId);
}
```

### Quota Limits by Plan

| Plan | Daily Limit |
|------|-------------|
| Free | 5 suggestions |
| Premium | 10 suggestions |

### Relevant Files

- `backend/src/main/java/com/fazquepaga/taskandpay/ai/AIQuota.java` [NEW]
- `backend/src/main/java/com/fazquepaga/taskandpay/ai/AIQuotaRepository.java` [NEW]
- `backend/src/main/java/com/fazquepaga/taskandpay/ai/AIQuotaService.java` [NEW]
- `backend/src/main/java/com/fazquepaga/taskandpay/controller/TaskController.java` [MODIFY]
- `frontend/src/locales/pt.json` [MODIFY]
- `frontend/src/locales/en.json` [MODIFY]

### Error Response Example

```json
{
  "error": "ai_quota_exceeded",
  "message": "Você atingiu o limite diário de sugestões. Tente novamente amanhã.",
  "remainingQuota": 0,
  "resetTime": "2024-01-02T00:00:00Z"
}
```

## Success Criteria

- [ ] Free users can only make 5 AI suggestions per day
- [ ] Premium users can only make 10 AI suggestions per day
- [ ] Quota resets automatically at midnight
- [ ] Friendly error message displayed when quota exceeded
- [ ] Quota usage is logged for analysis
- [ ] All tests pass
- [ ] Code is reviewed and approved
