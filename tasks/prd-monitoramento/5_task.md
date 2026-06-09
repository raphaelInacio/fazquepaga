## status: completed

<task_context>
<domain>backend/shared/stats</domain>
<type>implementation</type>
<scope>database</scope>
<complexity>medium</complexity>
<dependencies>database</dependencies>
</task_context>

# Task 5.0: Serviço de Contadores Analíticos (Firestore Stats)

## Overview

Implementar o serviço de atualização de métricas atômicas analíticas no Firestore para registrar contadores consolidados de tarefas (criadas, concluídas, aprovadas), valores financeiros de mesadas e uso de IA, de forma que o painel do pai consulte apenas um documento e não precise ler toda a coleção de tarefas.

<requirements>
- Criação de interface `StatsService` e implementação `FirestoreStatsService`.
- Uso de `FieldValue.increment()` nas escritas para evitar race conditions no Firestore.
- Integração da lógica de contadores atômicos nas seguintes transações do backend:
  - Criação de tarefas (`TaskService.createTask` -> incrementa `totalTasksCreated`).
  - Envio de conclusão de tarefas (`TaskService.completeTask` -> incrementa `totalTasksCompleted`).
  - Aprovação de tarefas pelo pai (`TaskService.approveTask` -> incrementa `totalTasksApproved` e soma `totalAllowancePaid`).
  - Geração de sugestão de tarefas por IA (`AIService.suggestTasks` -> incrementa `aiSuggestionsUsed`).
</requirements>

## Subtasks

- [x] 5.1 Criar a interface `StatsService.java` no pacote `com.fazquepaga.taskandpay.shared.stats`.
- [x] 5.2 Implementar a classe `FirestoreStatsService.java` usando o SDK Admin do Firestore.
- [x] 5.3 Modificar os fluxos de negócios nos serviços `TaskService` (ou respectivos handlers de transação) para disparar os incrementos.
- [x] 5.4 Modificar o serviço de IA `GoogleGenAiService` (ou correspondente) para incrementar o contador de uso de IA da família.
- [x] 5.5 Criar testes unitários `FirestoreStatsServiceTest.java` com emulador Firestore local para verificar incremento correto.

## Implementation Details

Consulte as seções "2. Estratégia de Análise Diretamente no Firestore" e "Core Interfaces" na [Especificação Técnica](file:///C:/Users/conta/developer/fazquepaga/tasks/prd-monitoramento/techspec.md#L33-L72).

### Relevant Files

- `backend/src/main/java/com/fazquepaga/taskandpay/shared/stats/StatsService.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/shared/stats/FirestoreStatsService.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/tasks/TaskService.java` (ou equivalente no seu projeto)
- `backend/src/main/java/com/fazquepaga/taskandpay/ai/GoogleGenAiService.java` (ou equivalente)

### Dependent Files

- Ninguém (Infraestrutura Firestore já existente)

## Success Criteria

- Ao criar, concluir ou aprovar uma tarefa, o documento `/families/{familyId}/metadata/stats` é criado ou atualizado atomicamente.
- O contador correspondente reflete a alteração exata no Firestore.
- A suite de testes unitários com emulador Firestore passa com sucesso.
