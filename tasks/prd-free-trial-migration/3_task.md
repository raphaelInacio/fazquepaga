---
status: completed
---

# Task 3.0: Frontend - Context e Estado de Trial

## Overview

Esta task atualiza o `SubscriptionContext` para gerenciar o estado do trial no frontend, consumindo os novos campos da API.

**MUST READ**: Antes de iniciar, revise as regras em:
- `docs/ai_guidance/rules/react.md`
- `docs/ai_guidance/rules/frontend-testing.md`

## Requirements

- Context deve expor `isTrialActive()`, `isTrialExpired()` e `trialDaysRemaining`
- Deve consumir os novos campos da API de status
- Deve atualizar o estado ao carregar o app

## Subtasks

- [ ] 3.1 Atualizar interface `SubscriptionContextType` com novos métodos
- [ ] 3.2 Adicionar estado local para `isTrialActive` e `trialDaysRemaining`
- [ ] 3.3 Modificar `reloadUser()` para popular o estado de trial
- [ ] 3.4 Implementar métodos `isTrialActive()` e `isTrialExpired()`
- [ ] 3.5 Atualizar o tipo de resposta em `subscriptionService.ts`

## Implementation Details

### SubscriptionContext.tsx

```typescript
interface SubscriptionContextType {
    // Existentes
    isPremium: () => boolean;
    canCreateTask: (count: number) => boolean;
    canUseAI: () => boolean;
    canAccessGiftCardStore: () => boolean;
    canAddChild: (count: number) => boolean;
    getMaxRecurringTasks: () => number;
    reloadUser: () => Promise<void>;
    // Novos
    isTrialActive: () => boolean;
    isTrialExpired: () => boolean;
    trialDaysRemaining: number | null;
}
```

```typescript
// Dentro do Provider
const [trialDaysRemaining, setTrialDaysRemaining] = useState<number | null>(null);
const [trialActive, setTrialActive] = useState<boolean>(false);

const isTrialActive = (): boolean => trialActive;
const isTrialExpired = (): boolean => !isPremium() && !trialActive;

const reloadUser = async () => {
    if (!isAuthenticated || !user) return;
    try {
        const status = await subscriptionService.getStatus();
        setTrialActive(status.isTrialActive);
        setTrialDaysRemaining(status.trialDaysRemaining);
        updateUser({
            ...user,
            subscriptionTier: status.tier,
            subscriptionStatus: status.status
        });
    } catch (error) {
        console.error("Failed to reload subscription status:", error);
    }
};
```

### Relevant Files

- `frontend/src/contexts/SubscriptionContext.tsx`
- `frontend/src/services/subscriptionService.ts`

## Success Criteria

- [ ] `isTrialActive()` retorna `true` durante trial ativo
- [ ] `isTrialExpired()` retorna `true` quando trial expirado
- [ ] `trialDaysRemaining` é atualizado corretamente
- [ ] Estado é carregado ao iniciar o app
- [ ] Código compila sem erros TypeScript
