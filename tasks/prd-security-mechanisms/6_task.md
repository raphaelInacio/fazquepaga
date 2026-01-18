---
status: done
---

# Task 6.0: Integration Testing and Verification

## Overview

Comprehensive testing and verification of all security mechanisms implemented in Tasks 1.0-5.0. This task ensures all components work correctly together and validates the security posture of the application.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- Integration tests for all security components
- End-to-end testing of security flows
- Performance validation (rate limit overhead)
- Documentation of security configuration
- Verification of monitoring and logging

## Subtasks

### Integration Tests

- [ ] 6.1 Create `RateLimitIntegrationTest` - verify 429 responses after limit exceeded
- [ ] 6.2 Create `AIQuotaIntegrationTest` - verify quota enforcement across requests
- [ ] 6.3 Create `RecaptchaIntegrationTest` - verify bot rejection (mocked reCAPTCHA)
- [ ] 6.4 Create `RefreshTokenIntegrationTest` - verify full refresh flow
- [ ] 6.5 Create `SecurityConfigIntegrationTest` - verify filter chain order

### End-to-End Tests

- [ ] 6.6 Write E2E test: user login with rate limiting
- [ ] 6.7 Write E2E test: AI suggestion with quota limit
- [ ] 6.8 Write E2E test: token refresh on expiration
- [ ] 6.9 Write E2E test: global logout

### Performance Validation

- [ ] 6.10 Measure latency overhead of rate limiting filter
- [ ] 6.11 Verify Caffeine cache memory usage under load
- [ ] 6.12 Validate Firestore read/write costs for quotas

### Documentation

- [ ] 6.13 Document security configuration in project docs
- [ ] 6.14 Create runbook for common security scenarios
- [ ] 6.15 Update API documentation with rate limit headers
- [ ] 6.16 Document reCAPTCHA setup process

### Monitoring Verification

- [ ] 6.17 Verify rate limit events are logged correctly
- [ ] 6.18 Verify AI quota events are logged correctly
- [ ] 6.19 Verify reCAPTCHA scores are logged
- [ ] 6.20 Test log queries in Cloud Logging

## Implementation Details

### From Tech Spec - Example Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
class RateLimitIntegrationTest {
    
    @Test
    void shouldReturn429_whenRateLimitExceeded() {
        // Make 11 requests in sequence
        // Assert: 10 pass, 11th returns 429
    }
}
```

### Test Scenarios

| Component | Happy Path | Edge Cases |
|-----------|------------|------------|
| Rate Limit | Request passes | Limit exceeded, bucket refills |
| AI Quota | Usage recorded | Quota exhausted, daily reset |
| reCAPTCHA | High score passes | Low score rejected, API error |
| Refresh Token | Token renewed | Token expired, token revoked |

### Relevant Files

- `backend/src/test/java/com/fazquepaga/taskandpay/security/RateLimitIntegrationTest.java` [NEW]
- `backend/src/test/java/com/fazquepaga/taskandpay/ai/AIQuotaIntegrationTest.java` [NEW]
- `backend/src/test/java/com/fazquepaga/taskandpay/security/RecaptchaIntegrationTest.java` [NEW]
- `backend/src/test/java/com/fazquepaga/taskandpay/security/RefreshTokenIntegrationTest.java` [NEW]
- `docs/security/README.md` [NEW]
- `docs/security/runbook.md` [NEW]

### Logging Verification Queries

```sql
-- Cloud Logging query for rate limit events
resource.type="cloud_run_revision"
jsonPayload.message:"Rate limit exceeded"

-- Cloud Logging query for AI quota events
resource.type="cloud_run_revision"
jsonPayload.message:"AI quota"

-- Cloud Logging query for reCAPTCHA scores
resource.type="cloud_run_revision"
jsonPayload.message:"reCAPTCHA"
```

## Success Criteria

- [ ] All integration tests pass
- [ ] E2E tests cover all security flows
- [ ] Rate limiting adds < 5ms latency overhead
- [ ] Security configuration is documented
- [ ] Logs are queryable in Cloud Logging
- [ ] All tests pass in CI/CD pipeline
- [ ] Code is reviewed and approved
