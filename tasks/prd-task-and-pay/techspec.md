# Especificação Técnica: Clean-up do MVP — Remoção de Features Não Finalizadas

## Resumo Executivo

Este documento especifica as mudanças técnicas necessárias para alinhar o código com o escopo do MVP definido no PRD. As três features fora do escopo — **WhatsApp**, **Gift Card Store** e **Prova de Foto (requiresProof)** — possuem código parcialmente implementado que precisa ser desativado ou removido para reduzir superfície de risco, simplificar o fluxo de tarefas e eliminar dependências externas desnecessárias (Twilio, RVHub).

A estratégia adotada é: **remover completamente** o `NotificationService` e sua infraestrutura de Pub/Sub; **desabilitar via feature flag** o módulo WhatsApp no backend (preservar para uso futuro); **ocultar** a rota `/gift-cards` no frontend mantendo o backend; e **neutralizar** o campo `requiresProof` fixando-o em `false` no backend.

---

## Arquitetura do Sistema

### Visão Geral dos Componentes Afetados

| Componente | Localização | Ação |
|---|---|---|
| `NotificationService` | `backend/notification/` | ❌ Remover (classes + beans + Pub/Sub topic) |
| `NotificationConfig` | `backend/notification/` | ❌ Remover (subscriber Pub/Sub) |
| `NotificationListener` | `backend/notification/` | ❌ Remover |
| `NotificationEvent` / `NotificationType` | `backend/notification/` | ❌ Remover |
| `WhatsAppController` | `backend/whatsapp/` | ⚠️ Desabilitar endpoint (`@ConditionalOnProperty`) |
| `WhatsAppService` / `WhatsAppClient` | `backend/whatsapp/` | ⚠️ Manter código, desabilitar bean |
| `GiftCardStorePage.tsx` | `frontend/pages/` | ⚠️ Desabilitar rota no `App.tsx` |
| `requiresProof` em `Task.java` | `backend/tasks/` | ⚠️ Campo preservado no modelo, sempre `false` |
| `TaskService.completeTask` | `backend/tasks/` | 🔧 Remover branch `requiresProof` → sempre aprovação direta |
| `NotificationService` calls em `TaskService` / `WithdrawalService` | `backend/tasks/` e `backend/allowance/` | 🔧 Remover chamadas |

---

## Design de Implementação

### 1. Remoção do NotificationService e Infraestrutura Pub/Sub

**Arquivos a deletar:**
- `NotificationService.java`
- `NotificationConfig.java`
- `NotificationListener.java`
- `NotificationEvent.java`
- `NotificationType.java`

**Impactos em cascata — remover todas as chamadas nos chamadores:**

```java
// TaskService.java — remover blocos try/catch de notificação:
// notificationService.sendTaskCompleted(task, child, parent);
// notificationService.sendTaskApproved(task, child);

// WithdrawalService.java — remover:
// notificationService.sendWithdrawalRequested(parent, child, amount);
// notificationService.sendWithdrawalPaid(child, transaction.getAmount());

// payment/SubscriptionService.java (verificar) — remover:
// notificationService.sendSubscriptionCanceled(...)
```

**`application.yml` / `application-prod.yml`**: remover propriedades:
- `pubsub.notification-topic`
- `pubsub.notification-subscription`

### 2. Desativação do WhatsApp via Feature Flag

Adicionar propriedade em `application.yml`:

```yaml
features:
  whatsapp:
    enabled: false
```

Aplicar `@ConditionalOnProperty(name = "features.whatsapp.enabled", havingValue = "true")` em:
- `WhatsAppController`
- `WhatsAppService` (bean)
- `TwilioWhatsAppClient` (bean)

O `MockWhatsAppClient` pode ser removido junto (apenas existe para testes do fluxo WhatsApp).

### 3. Desabilitar Rota `/gift-cards` no Frontend

```tsx
// App.tsx — comentar ou remover a Route:
// <Route path="/gift-cards" element={<ProtectedRoute><GiftCardStorePage /></ProtectedRoute>} />
```

Remover também o import de `GiftCardStorePage` e `GiftCardApprovalDialog` do `App.tsx` e do `Dashboard.tsx` (seção de pendingGiftCards). O backend (`GiftCardController`, `GiftCardService`) permanece intacto.

### 4. Neutralizar `requiresProof` no Fluxo de Tarefas

**`TaskService.createTask`**: forçar `requiresProof = false` independente do payload:

```java
Task task = Task.builder()
    // ...
    .requiresProof(false) // MVP: proof não suportado
    // ...
    .build();
```

**`TaskService.completeTask`**: remover o branch de `requiresProof`, sempre auto-aprovar:

```java
// Antes:
if (task.isRequiresProof()) {
    task.setStatus(Task.TaskStatus.PENDING_APPROVAL);
} else {
    task.setStatus(Task.TaskStatus.APPROVED);
    // ...
}

// Depois (MVP):
task.setStatus(Task.TaskStatus.APPROVED);
task.setAcknowledged(false);
// calcular valor e adicionar transação sempre
```

O campo `requiresProof` no modelo `Task.java` e no Firestore é preservado para compatibilidade (documentos existentes). O status `PENDING_APPROVAL` no enum `TaskStatus` pode ser mantido mas nunca será atribuído.

### Modelos de Dados

Nenhuma migração de Firestore é necessária. Os campos existentes são preservados; apenas a lógica de atribuição é alterada.

```
Task (Firestore — sem mudanças no schema)
 ├── requiresProof: Boolean  → sempre false para novas tarefas
 ├── aiValidated: Boolean    → campo preservado (para validação futura via portal)
 └── status: PENDING | COMPLETED | PENDING_APPROVAL | APPROVED
      → PENDING_APPROVAL nunca será atribuído no MVP
```

### Endpoints de API

Nenhum endpoint novo. Endpoints removidos/desativados:

| Método | Caminho | Ação |
|---|---|---|
| `POST` | `/api/v1/whatsapp/webhook` | Desativado via `@ConditionalOnProperty` |
| `GET/POST` | `/api/v1/gift-cards/**` | Backend mantido, rota de frontend removida |

---

## Pontos de Integração

### Twilio (WhatsApp)

- **Desativar**: bean `TwilioWhatsAppClient` com `@ConditionalOnProperty`
- **Remover**: variáveis de ambiente `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_PHONE_NUMBER` do `application.yml` (manter no GCP Secret Manager comentado, para restauração futura)

### Google Cloud Pub/Sub

- **Remover**: subscription `notification-subscription` e topic `notification-topic` do código
- **Manter**: a subscription `pubsub.topic-name` (usada para `ProofSubmittedEvent` pelo `WhatsAppService`) pode ser removida junto com o WhatsApp desativado
- **Ação GCP**: após deploy, deletar topics/subscriptions de notificação no projeto para eliminar cobranças

---

## Abordagem de Testes

### Testes Unitários

- **`TaskServiceTest`**: validar que `completeTask` nunca seta `PENDING_APPROVAL`; validar que `createTask` ignora `requiresProof = true` no request
- **Verificar ausência de chamadas a `NotificationService`**: garantir que nenhum teste depende de notificação via Pub/Sub/Twilio
- **`WhatsAppServiceTest`**: marcar como `@Disabled` ou deletar se o bean não for mais carregado

### Testes de Integração

- **`TaskControllerIT`**: verificar que `POST /tasks/{id}/complete` resulta em `APPROVED` diretamente
- **Context load test**: verificar que o contexto Spring sobe sem `WhatsAppController`, `NotificationService` e conexão com o Pub/Sub de notificação

### Testes E2E (Playwright)

- Verificar que a navegação para `/gift-cards` retorna 404 ou redireciona corretamente
- Verificar fluxo de conclusão de tarefa no Portal da Criança: tarefa passa de `PENDING` para `APPROVED` sem etapa intermediária de aprovação manual para tarefas sem proof

---

## Sequenciamento de Desenvolvimento

### Ordem de Construção

1. **Backend — Remover NotificationService** (sem dependências externas no próprio pacote): deletar as 5 classes e remover os imports/chamadas em `TaskService` e `WithdrawalService`. Executar `./mvnw spotless:apply` e garantir compilação.
2. **Backend — Feature Flag WhatsApp**: adicionar propriedade `features.whatsapp.enabled=false` e anotar `WhatsAppController` e `WhatsAppService` com `@ConditionalOnProperty`. Verificar que o contexto ainda sobe.
3. **Backend — Neutralizar `requiresProof`**: simplificar `TaskService.createTask` e `completeTask`. Executar testes unitários.
4. **Frontend — Remover rota `/gift-cards`**: comentar a `Route` em `App.tsx`, remover imports órfãos e o estado `pendingGiftCards` do `Dashboard.tsx`.
5. **Verificação integrada**: `./mvnw test` + `npm run test:e2e` para garantir que nenhum teste quebrou.
6. **Deploy**: aplicar o build no Cloud Run; verificar que o contexto de produção sobe sem erros de Pub/Sub subscription.

### Dependências Técnicas

- **GCP Pub/Sub**: após o deploy, desativar manualmente os topics/subscriptions de notificação no console GCP para evitar cobranças e erros de startup
- **Twilio**: nenhuma ação necessária em conta Twilio — apenas o código deixa de chamar a API

---

## Monitoramento e Observabilidade

- **Logs a remover**: linhas `"WhatsApp sent to {}"` e `"Notification published"` desaparecem naturalmente com a remoção das classes
- **Logs novos** (opcional): adicionar `log.debug("requiresProof ignored: MVP mode")` em `TaskService` se desejado para rastreabilidade
- **GCP Cloud Run Logs**: verificar ausência de erros de conexão ao `pubsub.notification-subscription` após deploy
- **Alertas**: remover qualquer alerta configurado no GCP sobre `notification-subscription` lag

---

## Considerações Técnicas

### Decisões Principais

| Decisão | Justificativa |
|---|---|
| Remover `NotificationService` completamente | Não há canal de notificação alternativo no MVP; manter o código aumentaria complexidade sem benefício |
| Desativar WhatsApp por feature flag e não deletar | O módulo pode ser reativado no futuro; o custo de manutenção é baixo |
| Preservar `requiresProof` no modelo mas fixar `false` | Evita migração do Firestore e mantém retrocompatibilidade |
| Ocultar `/gift-cards` apenas no frontend | O backend possui lógica de negócio valiosa (RVHub); preserva para Fase 2 |

### Riscos Conhecidos

| Risco | Mitigação |
|---|---|
| Remoção do `NotificationService` quebra testes que mockam a classe | Identificar e remover mocks nos testes de `TaskService` e `WithdrawalService` antes de compilar |
| `application-prod.yml` pode referenciar `pubsub.notification-subscription` | Verificar e remover antes do deploy para evitar `BeanCreationException` no startup |
| `Dashboard.tsx` referencia `pendingGiftCards` — remoção pode causar TypeScript error | Remover estado e todas as referências JSX junto com a rota |

### Conformidade com Skills Padrão

- **[use-java-spring-boot](.agent/rules/use-java-spring-boot.md)**: uso de `@ConditionalOnProperty` é padrão Spring Boot para feature flags
- **[code-standards](.agent/rules/code-standards.md)**: após mudanças, executar `./mvnw spotless:apply` (Google Java Format AOSP)
- **[tests](.agent/rules/tests.md)**: cobertura mínima de 60% deve ser mantida; testes de `TaskService` devem ser atualizados
- **[logging](.agent/rules/logging.md)**: remover logs que referenciam operações WhatsApp/Pub/Sub desativadas
- **[react](.agent/rules/react.md)**: remover imports não utilizados (`GiftCardStorePage`, `GiftCardApprovalDialog`) para evitar bundle desnecessário

### Arquivos Relevantes e Dependentes

**Backend (remover/modificar):**
- [`NotificationService.java`](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/notification/NotificationService.java)
- [`NotificationConfig.java`](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/notification/NotificationConfig.java)
- [`NotificationListener.java`](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/notification/NotificationListener.java)
- [`NotificationEvent.java`](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/notification/NotificationEvent.java)
- [`NotificationType.java`](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/notification/NotificationType.java)
- [`TaskService.java`](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/tasks/TaskService.java) — remover calls de notificação e simplificar `completeTask`
- [`WithdrawalService.java`](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/allowance/WithdrawalService.java) — remover calls de notificação
- [`WhatsAppController.java`](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/whatsapp/WhatsAppController.java) — adicionar `@ConditionalOnProperty`
- [`WhatsAppService.java`](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/whatsapp/WhatsAppService.java) — adicionar `@ConditionalOnProperty`

**Frontend (modificar):**
- [`App.tsx`](file:///c:/Users/conta/developer/fazquepaga/frontend/src/App.tsx) — remover import e `Route` de `GiftCardStorePage`
- [`Dashboard.tsx`](file:///c:/Users/conta/developer/fazquepaga/frontend/src/pages/Dashboard.tsx) — remover estado `pendingGiftCards`, import `GiftCardApprovalDialog`
