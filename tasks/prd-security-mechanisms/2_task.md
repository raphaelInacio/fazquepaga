---
status: pending
---

# Task 2.0: Rate Limiting Implementation (In-Memory with Caffeine)

## Overview

Implement rate limiting across the application to prevent abuse, brute force attacks, and control costs. This implementation uses Bucket4j with Caffeine cache (in-memory) to avoid Redis costs in MVP.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- Global rate limit by IP address (100 req/min)
- Per-user rate limit for authenticated requests (200 req/min)
- Specific limits for sensitive endpoints (auth: 10/min, AI: 5/min)
- HTTP 429 response with proper headers when limit exceeded
- Structured logging for blocked requests

## Subtasks

- [ ] 2.1 Add Bucket4j and Caffeine dependencies to `pom.xml`
- [ ] 2.2 Create `RateLimitService` interface and `CaffeineRateLimitService` implementation
- [ ] 2.3 Create `RateLimitConfig` configuration class with configurable limits
- [ ] 2.4 Create `RateLimitFilter` as a Spring `OncePerRequestFilter`
- [ ] 2.5 Configure rate limit buckets per endpoint type (global, auth, AI)
- [ ] 2.6 Register `RateLimitFilter` in `SecurityConfig.java`
- [ ] 2.7 Add rate limit configuration to `application.yml`
- [ ] 2.8 Implement HTTP 429 response with headers (`X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`, `Retry-After`)
- [ ] 2.9 Add structured logging for rate limit events
- [ ] 2.10 Write unit tests for `RateLimitService`
- [ ] 2.11 Write integration tests for `RateLimitFilter`

## Implementation Details

### From Tech Spec - Core Interfaces

```java
public interface RateLimitService {
    boolean tryConsume(String key, int tokens);
    long getAvailableTokens(String key);
}
```

### Rate Limit Configuration

| Endpoint Pattern | Limit | Per |
|------------------|-------|-----|
| Global (all) | 100 req/min | IP |
| `/api/v1/auth/**` | 10 req/min | IP |
| `/api/v1/children/login` | 10 req/min | IP |
| `/api/v1/tasks/suggest` | 5 req/min | User |
| `/api/v1/tasks/*/validate-image` | 3 req/min | User |
| Authenticated (general) | 200 req/min | User |

### Response Headers

```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1704067200
Retry-After: 30
```

### Relevant Files

- `backend/src/main/java/com/fazquepaga/taskandpay/security/RateLimitFilter.java` [NEW]
- `backend/src/main/java/com/fazquepaga/taskandpay/security/RateLimitService.java` [NEW]
- `backend/src/main/java/com/fazquepaga/taskandpay/security/RateLimitConfig.java` [NEW]
- `backend/src/main/java/com/fazquepaga/taskandpay/config/SecurityConfig.java` [MODIFY]
- `backend/src/main/resources/application.yml` [MODIFY]
- `backend/pom.xml` [MODIFY]

### Dependencies to Add

```xml
<!-- Rate Limiting -->
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>

<!-- Caffeine Cache -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
    <version>3.1.8</version>
</dependency>
```

## Success Criteria

- [ ] Rate limiting is active for all endpoints
- [ ] Sensitive endpoints have stricter limits
- [ ] HTTP 429 is returned with proper headers when limit exceeded
- [ ] Rate limit counters reset after time window
- [ ] Logs show blocked requests with IP, endpoint, and remaining tokens
- [ ] All tests pass
- [ ] Code is reviewed and approved
