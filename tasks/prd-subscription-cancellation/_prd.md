# PRD: Cancelamento de Assinatura

## Overview

O TaskAndPay oferece um plano Premium com recursos avançados (IA, filhos ilimitados, gift cards). Atualmente, não existe um fluxo self-service para os usuários cancelarem sua assinatura. Esta feature permitirá que pais/mães Premium cancelem sua assinatura de forma autônoma via interface web, com coleta de feedback sobre o motivo do cancelamento.

**Problema**: Usuários Premium não conseguem cancelar autonomamente, dependendo de suporte manual.

**Valor**: Autonomia do usuário, compliance com boas práticas de SaaS e coleta de dados para análise de churn.

## Goals

1. **Autonomia**: Fornecer fluxo de cancelamento claro, transparente e totalmente self-service
2. **Feedback**: Coletar motivo do cancelamento para análise de churn e melhoria do produto
3. **Transparência**: Informar claramente o impacto do cancelamento antes da confirmação
4. **Integração**: Manter consistência com Asaas via API e webhooks

## User Stories

- **Como pai/mãe Premium**, quero cancelar minha assinatura para não ser cobrado no próximo ciclo de cobrança
- **Como pai/mãe**, quero ser informado(a) do que perderei ao cancelar para tomar uma decisão consciente
- **Como pai/mãe**, quero fornecer o motivo do cancelamento para ajudar a melhorar o produto
- **Como pai/mãe**, quero receber confirmação do cancelamento via WhatsApp para ter registro da ação

## Core Features

### FR-1: Interface de Cancelamento
**O que faz**: Adiciona botão "Cancelar Assinatura" na página de configurações (Settings) para usuários Premium.
**Por que é importante**: Permite acesso fácil ao fluxo de cancelamento.
**Como funciona**: Botão visível apenas para usuários com assinatura ativa abre um fluxo em modal.

**Requisitos Funcionais:**
1. Exibir botão "Cancelar Assinatura" apenas para usuários Premium ativos
2. Botão deve abrir modal com fluxo de cancelamento
3. Botão deve usar estilo visual de ação destrutiva (vermelho)

---

### FR-2: Coleta de Motivo (Churn Survey)
**O que faz**: Apresenta tela com opções de motivo para cancelamento.
**Por que é importante**: Coleta dados para análise de churn e melhoria do produto.
**Como funciona**: Usuário seleciona uma opção ou escreve motivo livre.

**Requisitos Funcionais:**
1. Exibir opções pré-definidas:
   - "Muito caro"
   - "Não uso os recursos Premium"
   - "Encontrei alternativa melhor"
   - "Vou voltar depois"
   - "Outro" (habilita campo de texto livre)
2. Seleção de motivo é obrigatória para prosseguir
3. Armazenar motivo selecionado para analytics futura

---

### FR-3: Tela de Confirmação com Impacto
**O que faz**: Exibe resumo do impacto do cancelamento antes da confirmação final.
**Por que é importante**: Garante que o usuário entenda as consequências da ação.
**Como funciona**: Lista de recursos que serão perdidos e data de expiração do acesso.

**Requisitos Funcionais:**
1. Exibir lista de recursos que serão perdidos:
   - Limite de filhos: ilimitado → 1
   - Limite de tarefas recorrentes: ilimitado → 5
   - Acesso à IA (sugestões de tarefas): Perdido
   - Acesso à loja de Gift Cards: Perdido
2. Exibir data até quando o acesso Premium será mantido
3. Botão de confirmação final ("Confirmar Cancelamento")
4. Botão de voltar/cancelar a ação

---

### FR-4: Processamento do Cancelamento
**O que faz**: Processa o cancelamento na API Asaas e atualiza estado local.
**Por que é importante**: Garante que a assinatura seja efetivamente cancelada no gateway.
**Como funciona**: Chama DELETE na API Asaas e aguarda webhook para sincronização.

**Requisitos Funcionais:**
1. Chamar API Asaas (`DELETE /v3/subscriptions/{id}`) com subscriptionId do usuário
2. Atualizar status local para `CANCELED`
3. **Manter tier PREMIUM até fim do período pago** (não rebaixar imediatamente)
4. Registrar timestamp do cancelamento e motivo no Firestore
5. Tratar erros da API com feedback ao usuário

---

### FR-5: Notificação de Confirmação
**O que faz**: Envia notificação via WhatsApp confirmando o cancelamento.
**Por que é importante**: Fornece registro da ação e transparência.
**Como funciona**: Usa infraestrutura de notificação existente (Twilio).

**Requisitos Funcionais:**
1. Enviar mensagem WhatsApp para o número cadastrado do pai/mãe
2. Mensagem deve conter:
   - Confirmação do cancelamento
   - Data até quando o acesso Premium será mantido
3. Usar template de mensagem aprovado pelo WhatsApp

## User Experience

### Fluxo do Usuário
```
Settings → Botão "Cancelar Assinatura" → Modal: Motivo → Modal: Confirmação → Sucesso
```

### Considerações de Design
- **Modal de Motivo**: Design neutro, opções claras em lista
- **Modal de Confirmação**: Usar cores de warning (amarelo/laranja), listar impactos claramente
- **Botão de Confirmação Final**: Vermelho, texto explícito ("Confirmar Cancelamento")
- **Micro-copy**: Tom empático e transparente, sem linguagem agressiva

### Acessibilidade
- Todos os modais devem ser navegáveis por teclado
- Botões com aria-labels apropriados
- Contraste adequado para textos de aviso

## High-Level Technical Constraints

- **Integração obrigatória**: API Asaas (`DELETE /v3/subscriptions/{id}`)
- **Notificação**: Twilio WhatsApp (infraestrutura existente no módulo `notification`)
- **Persistência**: Firestore para armazenar motivo e timestamp de cancelamento
- **Sincronização**: Webhook Asaas existente para atualização final de status
- **Segurança**: Endpoint deve validar que o usuário autenticado é o dono da assinatura

## Non-Goals (Out of Scope)

- **Reembolso proporcional** do período não utilizado
- **Pausar assinatura** temporariamente
- **Oferta de retenção** com desconto (pode ser adicionado em fase futura)
- **Cancelamento via WhatsApp** (apenas via interface web)
- **Reativação de assinatura** cancelada (usuário deve criar nova assinatura)

## Phased Rollout Plan

### MVP (Esta Feature)
- Fluxo completo de cancelamento via Settings
- Coleta de motivo (churn survey)
- Tela de confirmação com impacto
- Notificação WhatsApp
- Integração com API Asaas

### Phase 2 (Futuro)
- Ofertas de retenção antes do cancelamento
- Dashboard de analytics de churn
- Fluxo de win-back para usuários cancelados

## Success Metrics

*Não definido* — Feature será lançada sem métricas específicas de sucesso por decisão do stakeholder.

## Risks and Mitigations

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| Usuário cancela por engano | Baixa | Médio | Tela de confirmação clara com impacto visível |
| Falha na API Asaas | Baixa | Alto | Tratamento de erro com retry, feedback ao usuário e rollback local |
| Aumento de churn após facilitar cancelamento | Média | Médio | Coleta de feedback para identificar problemas e melhorar produto |

## Open Questions

1. **Data de expiração**: Usar campo `nextDueDate` do Asaas ou calcular localmente com base no `subscriptionId`?
2. **Template WhatsApp**: Existe template aprovado para notificação de cancelamento ou precisa ser criado?

## Appendix

### Integração Asaas - Endpoint de Cancelamento

```
DELETE /v3/subscriptions/{id}
Response: { "deleted": true, "id": "sub_XXX" }
```

### Campos a adicionar no Firestore (users collection)
- `cancellationDate`: TIMESTAMP
- `cancellationReason`: STRING

### Referências
- [PRD Principal TaskAndPay](../prd-task-and-pay/_prd.md)
- [Tech Spec TaskAndPay](../prd-task-and-pay/_techspec.md)
