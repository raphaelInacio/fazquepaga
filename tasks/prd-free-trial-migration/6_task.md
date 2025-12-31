---
status: completed
---

# Task 6.0: Atualização da Documentação do Projeto

## Overview

Esta task atualiza a documentação do projeto para refletir a mudança do modelo Freemium para Free Trial. Inclui README, AGENTS.MD, PRD baseline e Tech Spec baseline.

**MUST READ**: Antes de iniciar, revise:
- `README.md`
- `AGENTS.MD`
- `tasks/prd-task-and-pay/_prd.md`
- `tasks/prd-task-and-pay/_techspec.md`

## Requirements

- Documentação deve refletir o novo modelo de monetização
- Remover referências ao plano "Free" com funcionalidades limitadas
- Documentar novo campo `trialStartDate`
- Atualizar seções de features/pricing

## Subtasks

- [ ] 6.1 Atualizar `README.md` - seção "Plans and Monetization"
- [ ] 6.2 Atualizar `README.md` - remover menções a "Free Plan" com limite de tarefas
- [ ] 6.3 Atualizar `AGENTS.MD` se houver referências ao modelo antigo
- [ ] 6.4 Atualizar `tasks/prd-task-and-pay/_prd.md` - seção de monetização e status
- [ ] 6.5 Atualizar `tasks/prd-task-and-pay/_techspec.md` - modelo de dados e campos do User
- [ ] 6.6 Revisar e validar que todas as referências ao modelo antigo foram removidas

## Implementation Details

### README.md - Seção a Atualizar

**Antes:**
```markdown
*   **Plans and Monetization (Freemium)**:
    *   **Free Plan**: Limited to 5 recurring tasks, 1 child.
    *   **Premium Plan**: Unlimited tasks, AI features, Gift Card Store.
```

**Depois:**
```markdown
*   **Plans and Monetization (Free Trial)**:
    *   **Free Trial**: 3-day full access to all features.
    *   **Premium Plan**: Paid subscription after trial with unlimited access.
```

### tasks/prd-task-and-pay/_prd.md - Seção a Atualizar

Atualizar a tabela de funcionalidades para refletir:
- `Planos e Monetização` → Free Trial de 3 dias + Assinatura

### tasks/prd-task-and-pay/_techspec.md - Modelo de Dados

Adicionar na seção de "Modelos de Dados (Firestore)":
```markdown
-   **Coleção:** `users`
    -   **Campos Adicionais:**
        -   `trialStartDate`: TIMESTAMP (Data de início do trial, UTC)
```

### Relevant Files

- `README.md`
- `AGENTS.MD`
- `tasks/prd-task-and-pay/_prd.md`
- `tasks/prd-task-and-pay/_techspec.md`

## Success Criteria

- [ ] README reflete modelo Free Trial
- [ ] Nenhuma referência a "Free Plan" com limite de 5 tarefas
- [ ] PRD baseline atualizado com novo modelo
- [ ] Tech Spec baseline inclui campo `trialStartDate`
- [ ] Documentação é consistente entre todos os arquivos
