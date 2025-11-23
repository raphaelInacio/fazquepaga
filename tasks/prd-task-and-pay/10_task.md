---
status: pending
---

# Task 10.0: Frontend: Implementar Extrato Financeiro com Insights de IA

## Overview

Para que pais e filhos entendam o valor gerado, precisamos de uma tela de extrato financeiro. Esta tarefa consiste em criar um componente ou página no frontend que consuma os dados do novo endpoint de `ledger` do backend e os exiba de forma clara, enriquecendo a visualização com insights gerados por IA.

**MUST READ**: Antes de começar, revise as regras de React em `docs/ai_guidance/rules/react.mdc`.

## Requirements

- O extrato deve exibir a data, a descrição da tarefa e o valor ganho para cada transação.
- O extrato deve exibir um saldo total.
- A interface deve ser simples e fácil de ler, com um espaço dedicado para os insights da IA.

## Subtasks

- [ ] 10.1 No `allowanceService.ts` (ou em um novo `ledgerService.ts`), adicionar uma função para chamar o novo endpoint `GET /api/v1/children/{childId}/ledger`.
- [ ] 10.2 Criar um novo componente, `FinancialLedger.tsx`. Este componente receberá o `childId` como prop.
- [ ] 10.3 Dentro do componente, fazer a chamada à API para buscar os dados do extrato e o saldo total.
- [ ] 10.4 Renderizar os dados em uma tabela ou lista. Utilize os componentes `Table` e `Card` de `frontend/src/components/ui/`.
- [ ] 10.5 **(Feature de IA)** Criar um endpoint no backend (ex: `GET /api/v1/children/{childId}/ledger/insights`) que usa a IA para analisar as transações e retornar uma dica ou observação em texto.
- [ ] 10.6 **(Feature de IA)** No frontend, chamar o novo endpoint de insights e exibir a mensagem em uma área de destaque no topo do extrato. Exemplo: "Notamos que Maria economizou 70% do que ganhou este mês. Ótimo progresso para a meta dela!".
- [ ] 10.7 Integrar o componente `FinancialLedger.tsx` na página `ChildTasks.tsx` ou em uma nova aba no Dashboard.
- [ ] 10.8 Implementar testes de componente para o extrato, incluindo a exibição do insight.

## Implementation Details

O endpoint de insights da IA pode ser simples inicialmente, gerando uma observação com base no percentual economizado vs. gasto. O saldo total pode ser calculado no frontend a partir das transações ou vir diretamente de um campo na resposta da API.

### Relevant Files

- `frontend/src/pages/ChildTasks.tsx`
- `frontend/src/services/allowanceService.ts`
- `frontend/src/components/ui/card.tsx`
- `frontend/src/components/ui/alert.tsx` (para o insight)
- `backend/src/main/java/com/fazquepaga/taskandpay/allowance/AllowanceController.java`

## Success Criteria

- O extrato financeiro é exibido corretamente.
- Uma dica ou insight gerado por IA é exibido no topo do extrato.
- A lista de tarefas aprovadas com seus valores é exibida em ordem cronológica.
- O saldo total é exibido corretamente.
- O código é revisado e aprovado.
- Todos os testes de UI passam.