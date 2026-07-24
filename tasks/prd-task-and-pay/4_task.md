# Tarefa 4.0: Frontend — Remover requiresProof e PENDING_APPROVAL da UI

<critical>Leia os arquivos _prd.md e techspec.md desta pasta antes de começar. Sua tarefa será invalidada se não o fizer.</critical>

## Visão Geral

Remove da UI todas as superfícies relacionadas ao envio de prova de foto e ao status intermediário `PENDING_APPROVAL`. As mudanças afetam:

- **`ChildTasks.tsx`**: remover o toggle "Requer Comprovante?" do formulário de criação de tarefas, remover a exibição de `proofImageUrl` no modal de revisão, e remover `PENDING_APPROVAL` do filtro de status.
- **`ChildPortal.tsx`**: remover a atualização de status local para `PENDING_APPROVAL` após concluir uma tarefa.
- **`types/index.ts`**: avaliar se campos `requiresProof` e `proofImageUrl` devem ser marcados como opcionais (mantê-los para compatibilidade com o backend, mas sem uso ativo na UI).

<skills>
### Conformidade com Skills Padrão

- **[react]**: remover estado e lógica condicional de UI que não tem mais propósito
- **[code-standards]**: zero erros TypeScript — não deixar variáveis não utilizadas
- **[internationalization]**: as chaves i18n (`proofRequired`, `viewProof`, `requiresProof`, `noProof`) serão removidas na Tarefa 5.0 — nesta tarefa, remover apenas os componentes que as consomem
</skills>

<requirements>
- Remover o toggle `requiresProof` do formulário de criação de tarefas em `ChildTasks.tsx`
- Remover a exibição de `proofImageUrl` e a seção de comprovante do modal de revisão em `ChildTasks.tsx`
- Remover a opção `PENDING_APPROVAL` do componente `<Select>` de filtro de status em `ChildTasks.tsx`
- Remover a lógica em `ChildPortal.tsx` que atualiza o status local para `PENDING_APPROVAL` após `completeTask`
- Remover a exibição de `proofImageUrl` (link "View Proof") do `Dashboard.tsx`
- `npm run build` deve concluir sem erros de TypeScript
</requirements>

## Subtarefas

- [ ] 4.1 Em `ChildTasks.tsx`:
  - Remover o campo `requiresProof` do estado inicial `newTask` (linha 40) e do objeto enviado ao `taskService.createTask` (linhas 190, 197, 233)
  - Remover o bloco JSX do toggle `id="requiresProof"` (linhas 739–749)
  - Remover a exibição condicional `{task.requiresProof && (...)}` no card de tarefa (linhas 511–515)
  - Remover o bloco condicional `{selectedTaskForReview?.requiresProof ? (...) : (...)}` do modal de revisão (linhas 600–622), substituindo por um elemento neutro ou removendo o bloco inteiramente
  - Remover `<SelectItem value="PENDING_APPROVAL">` do filtro de status (linha 408)
  - Remover a lógica `isPendingApproval` e seus usos na renderização do card (linha 438)
  - Remover o branch `case "PENDING_APPROVAL":` no switch de status (linha 245)
- [ ] 4.2 Em `ChildPortal.tsx`:
  - Remover a linha que atualiza o estado local para `PENDING_APPROVAL` após concluir tarefa (linha 92)
  - Garantir que o fluxo de conclusão apenas remove a tarefa da lista ou atualiza para `APPROVED`
- [ ] 4.3 Em `Dashboard.tsx`:
  - Remover o bloco `{item.task.proofImageUrl && (...)}` (linhas 636–639, "View Proof" link)
  - Verificar o filtro de `PENDING_APPROVAL` em `pendingTasks` (linha 110) — o filtro pode continuar existindo pois tarefas antigas no Firestore podem ter este status; avaliar se deve permanecer como fallback de leitura
- [ ] 4.4 Em `types/index.ts`: manter `requiresProof: boolean` e `proofImageUrl?: string` no tipo `Task` para compatibilidade com dados existentes no Firestore, mas marcar `requiresProof` como opcional (`requiresProof?: boolean`)
- [ ] 4.5 Executar `npm run build` — zero erros TypeScript/ESLint

## Detalhes de Implementação

Consulte a **seção "4. Neutralizar `requiresProof` no Fluxo de Tarefas"** da [Tech Spec](./techspec.md).

**Sobre o filtro `PENDING_APPROVAL` no Dashboard (subtarefa 4.3):**
Tarefas antigas no Firestore podem ter o status `PENDING_APPROVAL`. Para evitar que essas tarefas fiquem presas na fila de pendências do Dashboard para sempre, uma abordagem segura é manter o filtro de leitura no Dashboard (o pai ainda pode aprovar ou rejeitar essas tarefas antigas), mas **não** criar novas tarefas com esse status.

**Sobre o `ChildPortal.tsx` (subtarefa 4.2):**
Atualmente o portal atualiza o estado local de forma otimista para `PENDING_APPROVAL` após chamar `completeTask`. Com o backend retornando `APPROVED`, a atualização local deve refletir `APPROVED` ou simplesmente recarregar as tarefas via `loadTasks`.

## Critérios de Sucesso

- Formulário de criação de tarefas em `ChildTasks.tsx` não exibe o toggle "Requer Comprovante?"
- Modal de revisão de tarefas não exibe seção de foto de comprovante
- Filtro de status não contém opção `PENDING_APPROVAL`
- Dashboard não exibe link "View Proof" em nenhuma tarefa
- `npm run build` → zero erros

## Testes da Tarefa

- [ ] **Unitários/Componentes**: verificar que o formulário de criação não renderiza o campo `requiresProof`
- [ ] **E2E (Playwright)**: criar uma tarefa via `ChildTasks`, concluir via `ChildPortal`, verificar que o status vai direto para `APPROVED` sem etapa intermediária
- [ ] **Build**: `npm run build` sem erros TypeScript

<critical>SEMPRE CRIE E EXECUTE OS TESTES DA TAREFA ANTES DE CONSIDERÁ-LA FINALIZADA</critical>

## Arquivos Relevantes

- [`ChildTasks.tsx`](../../frontend/src/pages/ChildTasks.tsx) — **MODIFICAR** (remover requiresProof, PENDING_APPROVAL, proofImageUrl)
- [`ChildPortal.tsx`](../../frontend/src/pages/ChildPortal.tsx) — **MODIFICAR** (remover atualização local para PENDING_APPROVAL)
- [`Dashboard.tsx`](../../frontend/src/pages/Dashboard.tsx) — **MODIFICAR** (remover link "View Proof")
- [`types/index.ts`](../../frontend/src/types/index.ts) — **MODIFICAR** (tornar `requiresProof` opcional)
