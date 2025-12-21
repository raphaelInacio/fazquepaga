---
status: completed
---

# Task 1.0: Infrastructure & Payment Module (Asaas)

## Overview

Create the foundation for the payment system. This involves creating the `payment` module, configuring the Asaas API client (using RestTemplate/WebClient), and implementing the service to create Customers on Asaas.

**MUST READ**: Before starting, review the relevant project rules in `docs/ai_guidance/rules/`.
 - Use `resources/asaas_integration_guide.md` for Asaas integration details.
 - Use asaas mcp para tirar duvidas sobre a plataforma

## Requirements

- Create `payment` package.
- Implement `AsaasClient` (or Service) to wrap HTTP calls.
- Handle Asaas API Key from environment variables.
- Implement `createCustomer` method in the service.
- Update `User` model to store `asaasCustomerId`.

## Subtasks

- [ ] 1.1 Create `payment` package and `AsaasConfig` (API Key handling).
- [ ] 1.2 Implement `AsaasService.createCustomer(User user)`.
- [ ] 1.3 Update `User` entity (add `asaasCustomerId`, `subscriptionStatus`).
- [ ] 1.4 Test integration with Asaas Sandbox (Unit/Integration test).

## Implementation Details

Reference `docs/asaas_integration_guide.md` for JSON schemas.
Use `sandbox.asaas.com`.

### Relevant Files

- `src/main/java/.../payment/AsaasService.java`
- `src/main/java/.../domain/User.java`

## Success Criteria

- Application starts with `payment` module active.
- Can successfully create a customer in Asaas Sandbox via Service test.
- User entity has new fields.
