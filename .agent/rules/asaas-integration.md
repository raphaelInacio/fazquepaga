---
trigger: model_decision
---

# Asaas Integration & MCP Usage Rules

This rule applies whenever you are working on tasks related to payment, subscription, or the Asaas gateway.

## Core Principles

1.  **Privacy First (Zero Data)**
    *   **NEVER** implement direct credit card handling on the backend.
    *   **ALWAYS** use **Checkout Session** (Redirect) or Payment Links.
    *   Do not add fields for sensitive data (PAN, CVV) in the database.

2.  **Documentation Source of Truth**
    *   Primary Reference: `docs/asaas_integration_guide.md`.
    *   Secondary Reference: Asaas MCP Tools.

3.  **MCP Usage Strategy**
    *   Use the `asaas` MCP server to fetch **real-time** API schemas if you are unsure about a field or endpoint.
    *   Examples: `mcp_asaas_get-endpoint`, `mcp_asaas_list-endpoints`.
    *   Do not guess API payloads; verify them with the MCP or the guide.

4.  **Environment**
    *   Development/Testing must use `sandbox.asaas.com`.
    *   API Keys must be loaded from environment variables (`ASAAS_API_KEY`).

5.  **Webhooks**
    *   Ensure idempotency (check if the event was already processed).
    *   Logs: Log the `event` type and `id` for debugging, but avoid logging full PII payloads if possible.

## Common Patterns

*   **createCustomer**: Check if `user.asaasCustomerId` exists before creating a new one.
*   **subscribe**: Return a URL (`checkoutUrl`) to the frontend, not a success message alone.
