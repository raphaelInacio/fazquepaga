---
status: pending
---

# Task 14.0: Feature de IA: Implementar Relatórios de Comportamento para Pais

## Overview

Esta funcionalidade premium fornecerá aos pais relatórios semanais ou mensais gerados por IA, com insights sobre o progresso e comportamento de seus filhos. O objetivo é transformar dados brutos (tarefas concluídas/não concluídas) em informações acionáveis e fáceis de entender.

**MUST READ**: Antes de começar, revise as regras de design de API em `docs/ai_guidance/rules/api-rest-http.mdc`.

## Requirements

- O relatório deve ser apresentado em uma nova seção no Dashboard do pai.
- A IA deve gerar um resumo em texto, destacando pontos positivos e áreas de melhoria.
- A funcionalidade deve ser restrita a usuários com plano Premium.

## Subtasks

- [ ] 14.1 No backend, criar um novo endpoint, por exemplo, `GET /api/v1/children/{childId}/reports/summary`. O endpoint deve receber um período de tempo (ex: `last_7_days`).
- [ ] 14.2 Criar um novo serviço, `ReportService`, que busca todas as tarefas concluídas e pendentes no período.
- [ ] 14.3 No `ReportService`, agregar os dados (ex: taxa de conclusão, tarefas mais comuns, tarefas mais ignoradas) e enviá-los para um LLM com um prompt para gerar um resumo amigável. Ex: "Gere um resumo para um pai sobre o progresso de seu filho...".
- [ ] 14.4 No `SubscriptionService`, garantir que o acesso a este novo endpoint seja bloqueado para usuários do plano Free.
- [ ] 14.5 No frontend, criar um novo componente `ReportSummary.tsx`.
- [ ] 14.6 No `Dashboard.tsx`, chamar o novo endpoint e passar os dados para o componente `ReportSummary.tsx`.
- [ ] 14.7 O componente deve exibir o resumo de texto gerado pela IA em um componente `Card` ou `Alert`.
- [ ] 14.8 Implementar testes para o serviço de backend e para o componente de frontend.

## Implementation Details

O prompt para a IA deve ser cuidadosamente elaborado para focar em um tom positivo e construtivo. Ex: "Comece com um ponto positivo, depois mencione uma área para melhorar e termine com uma sugestão encorajadora."

A agregação de dados pode começar simples (taxa de conclusão geral) e evoluir para análises mais complexas (por tipo de tarefa, por dia da semana, etc.).

### Relevant Files

- `backend/src/main/java/com/fazquepaga/taskandpay/` (novo pacote `reports`)
- `backend/src/main/java/com/fazquepaga/taskandpay/subscription/SubscriptionService.java`
- `frontend/src/pages/Dashboard.tsx`
- `frontend/src/` (novo componente `components/ReportSummary.tsx`)
- `frontend/src/` (novo serviço `services/reportService.ts`)

## Success Criteria

- Pais com plano Premium podem ver um novo card em seu dashboard com um resumo em texto do progresso de seu filho.
- O texto é relevante e baseado nos dados reais de tarefas da criança.
- Usuários do plano Free não conseguem acessar esta funcionalidade.
- A funcionalidade é coberta por testes.
- O código é revisado e aprovado.
