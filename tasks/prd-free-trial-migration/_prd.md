# Documento de Requisitos do Produto (PRD): Migração para Free Trial de 3 Dias

## Visão Geral

Esta feature migra o modelo de monetização do TaskAndPay de **Freemium (Free + Premium)** para um modelo de **Free Trial de 3 dias** seguido de assinatura obrigatória.

**Problema:** O modelo Freemium atual pode gerar usuários que permanecem indefinidamente no plano gratuito sem nunca converter para o plano pago.

**Solução:** Oferecer acesso completo a todas as funcionalidades por 3 dias corridos. Após esse período, o usuário deve assinar o plano pago para continuar utilizando a plataforma. Um modal bloqueante será exibido após a expiração do trial.

## Objetivos

- **Objetivo Primário:** Aumentar a taxa de conversão para o plano pago através do modelo de trial.
- **Objetivo Secundário:** Simplificar o modelo de monetização (um único plano pago).
- **Métrica Principal:** Taxa de conversão trial → pago.

## Histórias de Usuário

### Como pai/mãe, eu quero...

1. **[TRIAL-01]** Ter acesso a todas as funcionalidades da plataforma durante um período de teste de 3 dias, para avaliar se o produto atende às minhas necessidades.
2. **[TRIAL-02]** Visualizar claramente quantos dias restam do meu período de teste, para me planejar quanto à assinatura.
3. **[TRIAL-03]** Ser informado de forma clara quando meu período de teste terminar, para entender que preciso assinar para continuar.
4. **[TRIAL-04]** Ter um caminho simples e direto para assinar o plano pago quando decidir continuar usando a plataforma.

### Como sistema...

5. **[TRIAL-05]** O sistema deve registrar a data de início do trial no momento da criação da conta do usuário.
6. **[TRIAL-06]** O sistema deve calcular automaticamente se o período de trial expirou (data atual > trialStartDate + 3 dias).
7. **[TRIAL-07]** O sistema deve bloquear totalmente o acesso às funcionalidades após a expiração do trial, exibindo um modal de assinatura.

## Funcionalidades Essenciais

### 1. Rastreamento do Período de Trial

| Requisito | Descrição |
|:----------|:----------|
| **FR-1.1** | Armazenar campo `trialStartDate` (timestamp) no documento do usuário |
| **FR-1.2** | O campo deve ser preenchido automaticamente no momento do registro |
| **FR-1.3** | O período de trial é de exatamente 72 horas (3 dias corridos) |

### 2. Verificação de Expiração

| Requisito | Descrição |
|:----------|:----------|
| **FR-2.1** | A cada acesso, verificar se `dataAtual > trialStartDate + 72h` |
| **FR-2.2** | Usuários com assinatura ativa (Premium) não são afetados pela verificação |
| **FR-2.3** | A verificação deve ocorrer no backend (API) e no frontend (UI) |

### 3. Indicador de Trial Ativo

| Requisito | Descrição |
|:----------|:----------|
| **FR-3.1** | Exibir badge/banner no header da aplicação durante o trial |
| **FR-3.2** | O indicador deve mostrar "Trial: X dias restantes" ou "Trial: Xh restantes" |
| **FR-3.3** | O indicador deve ser visível em todas as páginas do app |

### 4. Modal Bloqueante de Expiração

| Requisito | Descrição |
|:----------|:----------|
| **FR-4.1** | Exibir modal fullscreen quando o trial expirar |
| **FR-4.2** | O modal deve ser **bloqueante** (não pode ser fechado ou ignorado) |
| **FR-4.3** | O modal deve conter: título, mensagem sobre benefícios, botão CTA "Assinar Agora" |
| **FR-4.4** | O botão CTA deve redirecionar para o checkout Asaas existente |
| **FR-4.5** | O modal deve impedir navegação para qualquer outra página |

### 5. Remoção de Lógica de Plano Free

| Requisito | Descrição |
|:----------|:----------|
| **FR-5.1** | Remover verificações de "plano Free" vs "plano Premium" do frontend |
| **FR-5.2** | Remover restrições de funcionalidades baseadas em plano gratuito |
| **FR-5.3** | Manter apenas: trial ativo, trial expirado, ou assinante |

## Experiência do Usuário

### Durante o Trial (Dias 1-3)

- Badge no header: **"🎁 Trial: 2 dias restantes"**
- Acesso completo a todas as funcionalidades
- Nenhuma restrição de uso

### Trial Expirado (Dia 4+)

- **Modal Bloqueante:**
  ```
  ┌─────────────────────────────────────────┐
  │                                         │
  │     ⏰ Seu período de teste terminou    │
  │                                         │
  │   Continue aproveitando o TaskAndPay    │
  │   com todas as funcionalidades:         │
  │                                         │
  │   ✓ Tarefas ilimitadas                  │
  │   ✓ Sugestões de IA                     │
  │   ✓ Loja de Gift Cards                  │
  │   ✓ Relatórios financeiros              │
  │                                         │
  │        [  Assinar Agora  ]              │
  │                                         │
  └─────────────────────────────────────────┘
  ```
- Não é possível fechar o modal
- Não é possível acessar nenhuma funcionalidade

## Restrições Técnicas de Alto Nível

| Restrição | Descrição |
|:----------|:----------|
| **Integração Asaas** | Utilizar checkout Asaas existente para assinatura |
| **Webhooks** | Manter compatibilidade com webhooks de assinatura já implementados |
| **Fuso Horário** | Cálculo de expiração deve considerar UTC para consistência |
| **Autenticação** | Verificação de trial deve ocorrer após autenticação do usuário |

## Não-Objetivos (Fora do Escopo)

- ❌ Migração de usuários existentes (não existem usuários produtivos)
- ❌ Múltiplos planos de assinatura (apenas um plano)
- ❌ Extensão de trial por promoções ou cupons
- ❌ Notificações push/WhatsApp sobre expiração do trial
- ❌ Período de trial diferente por tipo de usuário

## Plano de Lançamento em Fases

### MVP (Fase 1)

- [ ] Campo `trialStartDate` no documento do usuário
- [ ] Verificação de expiração no backend
- [ ] Modal bloqueante no frontend
- [ ] Indicador de trial no header
- [ ] Integração com checkout Asaas
- [ ] Remoção de lógica de plano Free

### Fase 2 (Futuro - Opcional)

- [ ] Notificações de lembrete durante o trial ("Faltam 24h!")
- [ ] E-mail de boas-vindas com informações do trial
- [ ] Dashboard de métricas de conversão

## Métricas de Sucesso

| Métrica | Descrição | Meta Inicial |
|:--------|:----------|:-------------|
| **Taxa de Conversão** | % de usuários que assinam após trial | Baseline a definir |
| **Tempo Médio de Conversão** | Dias entre início do trial e assinatura | Baseline a definir |
| **Taxa de Abandono** | % de usuários que não retornam após expiração | Minimizar |

## Riscos e Mitigações

| Risco | Probabilidade | Impacto | Mitigação |
|:------|:--------------|:--------|:----------|
| Usuários desistem ao ver modal bloqueante | Média | Alto | Mensagem persuasiva destacando benefícios |
| Trial de 3 dias é curto demais | Baixa | Médio | Monitorar métricas, ajustar se necessário |
| Confusão sobre quando o trial termina | Baixa | Baixo | Indicador claro de dias restantes |

## Questões em Aberto

1. Qual o valor exato do plano de assinatura a ser exibido no modal?
2. Qual o texto/copy final do modal de expiração?
3. Devemos implementar algum tracking/analytics específico para funil de conversão?

## Referências

- [PRD Base - TaskAndPay](../../tasks/prd-task-and-pay/_prd.md)
- [Integração Asaas](../../docs/ai_guidance/rules/asaas-integration.md)
