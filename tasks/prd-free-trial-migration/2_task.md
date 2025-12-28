---
status: pending
---

# Task 2.0: Backend - API de Status de Trial

## Overview

Esta task atualiza a resposta do endpoint `/api/v1/subscription/status` para incluir informações do trial (`isTrialActive`, `trialDaysRemaining`).

**MUST READ**: Antes de iniciar, revise as regras em:
- `docs/ai_guidance/rules/api-rest-http.md`
- `docs/ai_guidance/rules/tests.md`

## Requirements

- `SubscriptionStatusResponse` deve incluir campos `isTrialActive` e `trialDaysRemaining`
- Controller deve usar os métodos implementados na Task 1.0
- API deve retornar valores corretos para cada estado do usuário

## Subtasks

- [ ] 2.1 Adicionar campos `isTrialActive` (boolean) e `trialDaysRemaining` (Integer) em `SubscriptionStatusResponse.java`
- [ ] 2.2 Modificar `SubscriptionController.java` para popular os novos campos na resposta
- [ ] 2.3 Adicionar testes para o endpoint `/api/v1/subscription/status`

## Implementation Details

### SubscriptionStatusResponse.java

```java
@Data
@Builder
public class SubscriptionStatusResponse {
    private User.SubscriptionTier tier;
    private User.SubscriptionStatus status;
    private String subscriptionId;
    // Novos campos
    private boolean isTrialActive;
    private Integer trialDaysRemaining;
}
```

### SubscriptionController.java

```java
@GetMapping("/status")
public ResponseEntity<SubscriptionStatusResponse> getStatus(
        @AuthenticationPrincipal User user) {
    User freshUser = subscriptionService.getUser(user.getId());
    boolean trialExpired = subscriptionService.isTrialExpired(freshUser);
    Integer daysRemaining = subscriptionService.getTrialDaysRemaining(freshUser);
    
    return ResponseEntity.ok(SubscriptionStatusResponse.builder()
            .tier(subscriptionService.getTier(user.getId()))
            .status(subscriptionService.getStatus(user.getId()))
            .isTrialActive(!trialExpired && freshUser.getSubscriptionTier() != User.SubscriptionTier.PREMIUM)
            .trialDaysRemaining(daysRemaining)
            .build());
}
```

### Relevant Files

- `backend/src/main/java/com/fazquepaga/taskandpay/subscription/dto/SubscriptionStatusResponse.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/subscription/SubscriptionController.java`

## Success Criteria

- [ ] Endpoint retorna `isTrialActive: true` para usuários em trial ativo
- [ ] Endpoint retorna `isTrialActive: false` para trial expirado
- [ ] Endpoint retorna `isTrialActive: false` para usuários Premium
- [ ] `trialDaysRemaining` mostra dias corretos durante trial
- [ ] `trialDaysRemaining` é `null` para usuários Premium
- [ ] Todos os testes passam
