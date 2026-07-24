# Tarefa 1.0: Backend — Remover NotificationService e Infraestrutura de Pub/Sub

<critical>Leia os arquivos _prd.md e techspec.md desta pasta antes de começar. Sua tarefa será invalidada se não o fizer.</critical>

## Visão Geral

Remove completamente o módulo `notification/` do backend — suas 5 classes, beans Spring, configuração de Pub/Sub e todos os pontos de chamada nos serviços que os consomem (`TaskService`, `WithdrawalService`). Após essa tarefa, o sistema não tentará mais publicar eventos no Pub/Sub de notificações nem enviar mensagens via Twilio/WhatsApp.

<skills>
### Conformidade com Skills Padrão

- **[use-java-spring-boot]**: remoção de beans Spring com segurança de injeção de dependências
- **[code-standards]**: executar `./mvnw spotless:apply` após cada alteração (Google Java Format AOSP)
- **[tests]**: cobertura mínima de 60% deve ser mantida após remoção
- **[logging]**: remover logs que referenciam operações de notificação desativadas
</skills>

<requirements>
- Deletar as 5 classes do pacote `notification/`: `NotificationService`, `NotificationConfig`, `NotificationListener`, `NotificationEvent`, `NotificationType`
- Remover todas as injeções e chamadas a `NotificationService` em `TaskService` e `WithdrawalService`
- Remover propriedades `pubsub.notification-topic` e `pubsub.notification-subscription` de todos os `application*.yml`
- Atualizar ou remover testes unitários que dependiam de mock de `NotificationService`
- O projeto deve compilar e todos os testes devem passar após a mudança
</requirements>

## Subtarefas

- [ ] 1.1 Deletar os 5 arquivos do pacote `notification/`
- [ ] 1.2 Remover injeção de `NotificationService` do construtor de `TaskService`; remover blocos `try/catch` de notificação em `approveTask` e `completeTask`
- [ ] 1.3 Remover injeção de `NotificationService` do construtor de `WithdrawalService`; remover chamadas `sendWithdrawalRequested` e `sendWithdrawalPaid`
- [ ] 1.4 Remover propriedades `pubsub.notification-topic` e `pubsub.notification-subscription` de `application.yml` (e variantes de perfil)
- [ ] 1.5 Atualizar testes: remover mocks de `NotificationService` onde existirem; garantir `./mvnw test` passa
- [ ] 1.6 Executar `./mvnw spotless:apply` e confirmar compilação limpa

## Detalhes de Implementação

Consulte a **seção "1. Remoção do NotificationService e Infraestrutura Pub/Sub"** da [Tech Spec](./techspec.md) para a lista completa de impactos em cascata.

**Pontos de atenção:**
- `NotificationListener` injeta `WhatsAppClient` — ao remover o listener, o `WhatsAppClient` deixa de ser referenciado neste pacote (mas continua existindo no pacote `whatsapp/`)
- Verificar se algum teste de integração inicializa o contexto completo e espera os beans de notification — esses testes devem ser atualizados

## Critérios de Sucesso

- `./mvnw clean compile` → BUILD SUCCESS, sem erros de import
- `./mvnw test` → todos os testes passam
- Nenhuma referência a `NotificationService`, `NotificationConfig`, `NotificationListener` resta no código compilado
- Cobertura JaCoCo ≥ 60%

## Testes da Tarefa

- [ ] **Unitários**: `TaskServiceTest` e `WithdrawalServiceTest` — sem mocks de `NotificationService`; fluxos de `completeTask` e `approveTask` continuam funcionando
- [ ] **Integração**: contexto Spring sobe sem os beans do pacote `notification/`
- [ ] **E2E**: não aplicável nesta tarefa

<critical>SEMPRE CRIE E EXECUTE OS TESTES DA TAREFA ANTES DE CONSIDERÁ-LA FINALIZADA</critical>

## Arquivos Relevantes

- [`NotificationService.java`](../../backend/src/main/java/com/fazquepaga/taskandpay/notification/NotificationService.java) — **DELETAR**
- [`NotificationConfig.java`](../../backend/src/main/java/com/fazquepaga/taskandpay/notification/NotificationConfig.java) — **DELETAR**
- [`NotificationListener.java`](../../backend/src/main/java/com/fazquepaga/taskandpay/notification/NotificationListener.java) — **DELETAR**
- [`NotificationEvent.java`](../../backend/src/main/java/com/fazquepaga/taskandpay/notification/NotificationEvent.java) — **DELETAR**
- [`NotificationType.java`](../../backend/src/main/java/com/fazquepaga/taskandpay/notification/NotificationType.java) — **DELETAR**
- [`TaskService.java`](../../backend/src/main/java/com/fazquepaga/taskandpay/tasks/TaskService.java) — **MODIFICAR** (remover injeção + chamadas)
- [`WithdrawalService.java`](../../backend/src/main/java/com/fazquepaga/taskandpay/allowance/WithdrawalService.java) — **MODIFICAR** (remover injeção + chamadas)
- `backend/src/main/resources/application.yml` — **MODIFICAR** (remover propriedades notification)
