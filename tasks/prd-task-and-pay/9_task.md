---
status: pending
---

# Task 9.0: Finalização do Frontend: Implementar Fluxo de Aprovação de Tarefas para Pais

## Overview

Esta é a tarefa mais crítica para completar o fluxo de valor do produto. Vamos construir a interface no portal dos pais que lhes permita ver as tarefas que seus filhos marcaram como concluídas, visualizar a prova (se houver) e aprovar o pagamento. Esta tarefa também inclui a refatoração do código para usar os novos endpoints da API criados na Tarefa 8.0, eliminando a dependência do `localStorage`.

**MUST READ**: Antes de começar, revise as regras de React em `docs/ai_guidance/rules/react.mdc`.

## Requirements

- A interface deve ser clara e intuitiva para um usuário não-técnico.
- A aprovação de uma tarefa deve fornecer feedback visual imediato ao usuário.
- O código deve ser refatorado para usar os novos endpoints da API para buscar dados, com tratamento adequado de estados de carregamento e erro.

## Subtasks

- [ ] 9.1 Na página `ChildTasks.tsx`, criar uma nova seção ou aba para "Tarefas Pendentes de Aprovação".
- [ ] 9.2 Para cada tarefa pendente, exibir os detalhes e um botão "Revisar". Se a tarefa tiver uma imagem de prova (`proofImageUrl`), exibi-la em um modal ou área de destaque.
- [ ] 9.3 **(Feature de IA)** No modal de revisão, se a tarefa foi pré-validada pela IA (`aiValidated`), exibir uma mensagem como "✅ A nossa IA pré-validou esta imagem e acredita que a tarefa foi concluída."
- [ ] 9.4 Implementar a função no `taskService.ts` para chamar a nova API `POST /api/v1/tasks/{taskId}/approve`.
- [ ] 9.5 Conectar o botão de aprovação na UI para chamar a função do serviço e, em caso de sucesso, mover a tarefa da lista de "pendentes" para a de "concluídas".
- [ ] 9.6 Refatorar `ChildTasks.tsx` e `Dashboard.tsx` para usar o novo endpoint `GET /api/v1/children/{childId}` em vez de depender de dados do `localStorage`. Isso envolverá a atualização do `childService.ts`.
- [ ] 9.7 Implementar testes de componentes para o novo fluxo de aprovação.

## Implementation Details

A página `ChildTasks.tsx` é o local central para esta funcionalidade. Use os componentes de UI existentes em `frontend/src/components/ui/` para construir a interface (ex: `Card`, `Button`, `Dialog` para o modal da imagem).

O `localStorage` deve ser completamente removido como meio de passar estado entre rotas. A busca de dados deve ser feita através dos serviços em `frontend/src/services/`.

### Relevant Files

- `frontend/src/pages/ChildTasks.tsx`
- `frontend/src/pages/Dashboard.tsx`
- `frontend/src/services/taskService.ts`
- `frontend/src/services/childService.ts`

## Success Criteria

- Um pai pode ver uma lista de tarefas enviadas por um filho.
- Um pai pode clicar em uma tarefa, ver a foto (se houver), e clicar em "Aprovar".
- A indicação da pré-validação da IA é exibida quando aplicável.
- Após a aprovação, a tarefa desaparece da lista de pendentes.
- Os dados da criança são carregados via API, e a dependência do `localStorage` é removida.
- O código é revisado e aprovado.
- Todos os testes de UI e E2E passam.