---
status: completed
---

# Task 1.0: JWT Service Refactoring and Secret Management

## Overview

This task focuses on hardening the existing JWT authentication by moving secrets out of source code and reducing token lifetimes. This is a critical security improvement that addresses the vulnerabilities identified in the security review.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.

## Requirements

- JWT signing secret must not be hardcoded in source code
- Token TTL for child tokens must be reduced from 1 year to 30 days
- All tokens must include `iat` (issued at) claim
- Configuration must be environment-specific (local, homolog, production)

## Subtasks

- [ ] 1.1 Add Spring Cloud GCP Secret Manager dependency to `pom.xml`
- [ ] 1.2 Create secret `jwt-signing-key` in GCP Secret Manager (document the process)
- [ ] 1.3 Update `JwtService.java` to read secret from environment variable or Secret Manager
- [ ] 1.4 Update `application.yml` to reference Secret Manager: `jwt.secret: ${sm://jwt-signing-key}`
- [ ] 1.5 Add fallback for local development using environment variable `JWT_SECRET`
- [ ] 1.6 Reduce child token TTL from 1 year to 30 days in `JwtService.java`
- [ ] 1.7 Ensure `iat` claim is included in all generated tokens
- [ ] 1.8 Update relevant unit tests in `JwtServiceTest.java`
- [ ] 1.9 Verify application startup with new configuration in local environment

## Implementation Details

### From Tech Spec - JwtService Refactor

```java
// application.yml configuration
jwt:
  secret: ${sm://jwt-signing-key}  # Reference to Secret Manager
  child-token-ttl: 30d            # Reduced from 1 year
```

### Relevant Files

- `backend/src/main/java/com/fazquepaga/taskandpay/security/JwtService.java`
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-local.yml`
- `backend/pom.xml`

### Dependencies to Add

```xml
<!-- GCP Secret Manager -->
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>spring-cloud-gcp-starter-secretmanager</artifactId>
</dependency>
```

## Success Criteria

- [ ] Application starts successfully with JWT secret from Secret Manager in production
- [ ] Local development works with environment variable fallback
- [ ] No hardcoded secrets in source code
- [ ] Child tokens expire after 30 days
- [ ] All tokens include `iat` claim
- [ ] All existing JWT-related tests pass
- [ ] Code is reviewed and approved
