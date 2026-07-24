# Tarefa 2.0: Backend — Desabilitar WhatsApp e Neutralizar requiresProof

<critical>Leia os arquivos _prd.md e techspec.md desta pasta antes de começar. Sua tarefa será invalidada se não o fizer.</critical>

## Visão Geral

Duas mudanças independentes no backend para alinhar com o MVP:

1. **WhatsApp**: O módulo `whatsapp/` é **preservado** no código, mas seus beans Spring são desativados via `@ConditionalOnProperty`. O endpoint `/api/v1/whatsapp/webhook` deixa de existir em runtime sem remover o código (reativável no futuro com uma flag).

2. **requiresProof**: O campo continua no modelo `Task` e no Firestore, mas em `TaskService` é forçado como `false` na criação, e o fluxo de `completeTask` é simplificado para sempre resultar em `APPROVED` diretamente — nunca mais `PENDING_APPROVAL`.

<skills>
### Conformidade com Skills Padrão

- **[use-java-spring-boot]**: `@ConditionalOnProperty` é o padrão Spring Boot para feature flags sem remoção de código
- **[code-standards]**: `./mvnw spotless:apply` após cada alteração
- **[tests]**: testes de `TaskService` devem cobrir o novo comportamento de `completeTask`
- **[firestore-nosql]**: nenhuma migração necessária — campo `requiresProof` permanece no schema
</skills>

<requirements>
- Adicionar `features.whatsapp.enabled=false` em `application.yml` e `application-prod.yml`
- Anotar `WhatsAppController` e `WhatsAppService` com `@ConditionalOnProperty(name = "features.whatsapp.enabled", havingValue = "true")`
- Em `TaskService.createTask`: ignorar o valor de `requiresProof` vindo do request — sempre usar `false`
- Em `TaskService.completeTask`: remover o branch `if (task.isRequiresProof())` — sempre setar `APPROVED` e registrar transação
- Testes unitários devem validar que `completeTask` nunca seta `PENDING_APPROVAL`
- O contexto Spring deve subir sem os beans WhatsApp ativos
</requirements>

## Subtarefas

- [ ] 2.1 Adicionar `features.whatsapp.enabled: false` em `application.yml` (e `application-prod.yml` se existir)
- [ ] 2.2 Anotar `@ConditionalOnProperty(name = "features.whatsapp.enabled", havingValue = "true")` em `WhatsAppController` e `@Service` do `WhatsAppService`
- [ ] 2.3 Em `TaskService.createTask`: substituir `request.isRequiresProof()` por `false` no builder de `Task`
- [ ] 2.4 Em `TaskService.completeTask`: remover o bloco `if/else` de `requiresProof` — manter apenas o ramo de auto-aprovação (status `APPROVED`, calcular valor, adicionar transação no ledger, incrementar stats)
- [ ] 2.5 Atualizar testes unitários de `TaskService`: verificar que `completeTask` retorna tarefa com status `APPROVED` independentemente de qualquer configuração de proof
- [ ] 2.6 Executar `./mvnw spotless:apply` e `./mvnw test`

## Detalhes de Implementação

Consulte as **seções "2. Desativação do WhatsApp via Feature Flag"** e **"4. Neutralizar requiresProof no Fluxo de Tarefas"** da [Tech Spec](./techspec.md).

**Trecho de referência — `TaskService.completeTask` após simplificação:**
```java
// MVP: sem requiresProof — sempre aprovar diretamente
task.setStatus(Task.TaskStatus.APPROVED);
task.setAcknowledged(false);

java.math.BigDecimal value =
    allowanceServiceProvider.get().calculateValueForTask(childId, taskId);
ledgerService.addTransaction(
    childId,
    value,
    "Task completed: " + task.getDescription(),
    Transaction.TransactionType.CREDIT);

statsService.incrementFamilyStat(familyId, "totalTasksApproved", 1);
// ... restante do método
```

**Atenção**: O status `PENDING_APPROVAL` permanece no enum `TaskStatus` para não quebrar desserialização de documentos Firestore antigos — apenas não é mais atribuído pelo código.

## Critérios de Sucesso

- `GET /api/v1/whatsapp/webhook` retorna 404 com `features.whatsapp.enabled=false`
- `POST /api/v1/tasks/{id}/complete` retorna task com `status: APPROVED` em todos os cenários
- `./mvnw test` passa com cobertura ≥ 60%

## Testes da Tarefa

- [ ] **Unitários**: `TaskServiceTest.completeTask_shouldAlwaysApproveDirectly()` — sem `PENDING_APPROVAL`
- [ ] **Unitários**: `TaskServiceTest.createTask_shouldIgnoreRequiresProofFromRequest()`
- [ ] **Integração**: contexto Spring sobe sem `WhatsAppController` e `WhatsAppService` nos beans ativos
- [ ] **E2E**: não aplicável nesta tarefa

<critical>SEMPRE CRIE E EXECUTE OS TESTES DA TAREFA ANTES DE CONSIDERÁ-LA FINALIZADA</critical>

## Arquivos Relevantes

- [`WhatsAppController.java`](../../backend/src/main/java/com/fazquepaga/taskandpay/whatsapp/WhatsAppController.java) — **MODIFICAR** (adicionar `@ConditionalOnProperty`)
- [`WhatsAppService.java`](../../backend/src/main/java/com/fazquepaga/taskandpay/whatsapp/WhatsAppService.java) — **MODIFICAR** (adicionar `@ConditionalOnProperty`)
- [`TaskService.java`](../../backend/src/main/java/com/fazquepaga/taskandpay/tasks/TaskService.java) — **MODIFICAR** (`createTask` + `completeTask`)
- `backend/src/main/resources/application.yml` — **MODIFICAR** (adicionar feature flag)
- `backend/src/test/java/.../tasks/TaskServiceTest.java` — **MODIFICAR** (atualizar testes)
