## status: pending

<task_context>
<domain>frontend/dashboard</domain>
<type>implementation</type>
<scope>performance</scope>
<complexity>medium</complexity>
<dependencies>database</dependencies>
</task_context>

# Task 6.0: Painel e Dashboard Otimizado no Frontend

## Overview

Ajustar a página do Dashboard do Pai no frontend React para consumir o documento consolidado de estatísticas `/families/{familyId}/metadata/stats` do Firestore de forma assíncrona, reduzindo a latência da tela e cortando o custo de leitura de coleções inteiras de tarefas.

<requirements>
- Alteração da lógica de dados na página Dashboard para carregar o documento de estatísticas consolidado.
- Exibição de: total de tarefas concluídas pendentes de aprovação, total acumulado ganho pelo filho, e contagem de cotas de IA usadas.
- O carregamento da página deve ler apenas o documento `/families/{familyId}/metadata/stats`.
</requirements>

## Subtasks

- [ ] 6.1 Mapear o tipo TypeScript correspondente ao documento `stats` no frontend.
- [ ] 6.2 Modificar a rotina de busca de dados analíticos em `frontend/src/pages/Dashboard.tsx` para fazer a leitura do documento consolidado.
- [ ] 6.3 Adaptar a UI do dashboard para exibir estes valores de forma limpa e harmônica (seguindo os padrões visuais premium do projeto).
- [ ] 6.4 Desenhar tratamentos visuais (loaders e fallbacks) enquanto o Firestore responde.
- [ ] 6.5 Criar um teste E2E com Playwright em `frontend/e2e/jornada-responsavel.spec.ts` validando a atualização em tempo real dos contadores analíticos no Dashboard após a conclusão e aprovação de tarefas.

## Implementation Details

Consulte a seção "Dashboards Estratégicos no Looker Studio" (adaptada para Firestore) e "Estratégia de Análise Diretamente no Firestore" na [Especificação Técnica](file:///C:/Users/conta/developer/fazquepaga/tasks/prd-monitoramento/techspec.md#L33-L72).

### Relevant Files

- `frontend/src/pages/Dashboard.tsx`
- `frontend/e2e/jornada-responsavel.spec.ts`

### Dependent Files

- `tasks/prd-monitoramento/5_task.md` (Depende do FirestoreStatsService gravando os dados consolidados no Firestore).

## Success Criteria

- A página Dashboard do Pai renderiza os totais corretamente lendo um único documento.
- O carregamento inicial da página do dashboard do pai cai de vários segundos (que realizava varredura de coleções) para menos de 100ms (uma única leitura direta de documento).
- Os testes E2E do Playwright passam com sucesso.
