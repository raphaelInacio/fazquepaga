---
status: pending
---

# Task 1.0: Backend - Modelo de Dados e Lógica de Trial

## Overview

Esta task adiciona o campo `trialStartDate` ao modelo `User` e implementa a lógica de verificação de expiração do trial no `SubscriptionService`. É a base para todo o sistema de trial.

**MUST READ**: Antes de iniciar, revise as regras em:
- `docs/ai_guidance/rules/use-java-spring-boot.md`
- `docs/ai_guidance/rules/firestore-nosql.md`
- `docs/ai_guidance/rules/tests.md`

## Requirements

- Campo `trialStartDate` deve ser um `Instant` (UTC)
- Novos usuários devem ter `trialStartDate` definido automaticamente no registro
- Métodos `isTrialExpired()` e `getTrialDaysRemaining()` devem estar no `SubscriptionService`
- Trial dura exatamente 72 horas (3 dias corridos)

## Subtasks

- [ ] 1.1 Adicionar campo `trialStartDate` do tipo `Instant` em `User.java`
- [ ] 1.2 Modificar `IdentityService.java` para definir `trialStartDate = Instant.now()` no registro de novos usuários
- [ ] 1.3 Implementar método `isTrialExpired(User user)` em `SubscriptionService.java`
- [ ] 1.4 Implementar método `getTrialDaysRemaining(User user)` em `SubscriptionService.java`
- [ ] 1.5 Criar testes unitários para os novos métodos em `SubscriptionServiceTest.java`

## Implementation Details

### User.java

```java
// Adicionar campo
private java.time.Instant trialStartDate;
```

### SubscriptionService.java

```java
public boolean isTrialExpired(User user) {
    if (isPremium(user)) return false;
    if (user.getTrialStartDate() == null) return true;
    
    Instant trialEnd = user.getTrialStartDate().plus(3, ChronoUnit.DAYS);
    return Instant.now().isAfter(trialEnd);
}

public Integer getTrialDaysRemaining(User user) {
    if (isPremium(user)) return null;
    if (user.getTrialStartDate() == null) return 0;
    
    Instant trialEnd = user.getTrialStartDate().plus(3, ChronoUnit.DAYS);
    long hours = ChronoUnit.HOURS.between(Instant.now(), trialEnd);
    if (hours <= 0) return 0;
    return (int) Math.ceil(hours / 24.0);
}
```

### Relevant Files

- `backend/src/main/java/com/fazquepaga/taskandpay/identity/User.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/identity/IdentityService.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/subscription/SubscriptionService.java`
- `backend/src/test/java/com/fazquepaga/taskandpay/subscription/SubscriptionServiceTest.java`

## Success Criteria

- [ ] Novos usuários registrados têm `trialStartDate` preenchido
- [ ] `isTrialExpired()` retorna `false` para usuários dentro do trial
- [ ] `isTrialExpired()` retorna `true` para usuários após 72h
- [ ] `isTrialExpired()` retorna `false` para usuários Premium (independente do trial)
- [ ] `getTrialDaysRemaining()` retorna dias corretos (1, 2 ou 3)
- [ ] Todos os testes unitários passam
- [ ] Cobertura de código ≥ 60%
