# Tarefa 6.0: Verificação Final — Testes E2E e Validação do Fluxo Completo do MVP

<critical>Leia os arquivos _prd.md e techspec.md desta pasta antes de começar. Sua tarefa será invalidada se não o fizer.</critical>

## Visão Geral

Tarefa de validação final que confirma que todas as mudanças das Tarefas 1.0 a 5.0 estão corretas, integradas e não quebraram nenhum comportamento existente do MVP. O objetivo é garantir que o fluxo principal do produto funciona ponta-a-ponta: pai cria tarefa → filho conclui → tarefa aprovada automaticamente → saldo atualizado — tudo sem qualquer referência às features removidas.

Esta tarefa também inclui a limpeza de configuração de produção (remoção de propriedades Twilio/notification obsoletas do `application-prod.yml` e validação dos segredos no GCP).

<skills>
### Conformidade com Skills Padrão

- **[e2e-testing]**: testes E2E com Playwright validam fluxos críticos do MVP
- **[tests]**: cobertura JaCoCo ≥ 60% no backend
- **[logging]**: verificar que nenhum log de erro de Pub/Sub aparece no startup do backend
- **[use-java-spring-boot]**: verificar que o contexto Spring sobe sem `BeanCreationException`
</skills>

<requirements>
- `./mvnw test` passa com cobertura ≥ 60% (JaCoCo)
- `npm run build` no frontend sem erros TypeScript
- `npm run test:e2e` passa — todos os testes Playwright existentes que não foram deletados devem continuar passando
- O contexto Spring sobe em produção sem erros relacionados a Pub/Sub de notificação
- `application-prod.yml` não contém propriedades `pubsub.notification-*` nem credenciais Twilio ativas
- Fluxo completo do MVP funciona end-to-end
</requirements>

## Subtarefas

- [ ] 6.1 Executar `./mvnw clean test` no backend — verificar BUILD SUCCESS e cobertura JaCoCo ≥ 60%
- [ ] 6.2 Executar `npm run build` no frontend — verificar zero erros TypeScript e ESLint
- [ ] 6.3 Executar `npm run test:e2e` — todos os testes Playwright existentes devem passar (exceto os deletados na Tarefa 3.0)
- [ ] 6.4 Verificar `application-prod.yml` (se existir) e garantir ausência de `pubsub.notification-topic`, `pubsub.notification-subscription`, `TWILIO_*`
- [ ] 6.5 Validação manual do fluxo MVP ponta-a-ponta (ver detalhes abaixo)
- [ ] 6.6 Verificar no GCP Console que os Pub/Sub topics/subscriptions de notificação podem ser desativados (ação manual no GCP — documentar como pendência se não for possível fazer via código)

## Detalhes de Implementação

Consulte a **seção "Sequenciamento de Desenvolvimento"** e **"Monitoramento e Observabilidade"** da [Tech Spec](./techspec.md).

### Fluxo de Validação Manual (subtarefa 6.5)

Execute o seguinte roteiro no ambiente local ou de staging:

**Fluxo Pai:**
1. Login como pai → Dashboard carrega sem botão "Loja de Recompensas" ✓
2. Navegar para `/gift-cards` → tela NotFound ✓
3. Acessar `/subscription` → sem menção a "WhatsApp" ou "Gift Cards" nos benefícios ✓
4. Criar uma tarefa para um filho → formulário sem toggle "Requer Comprovante?" ✓
5. Verificar no Dashboard que a tarefa aparece como `PENDING`

**Fluxo Filho:**
6. Login como filho → ChildPortal sem card/botão de Gift Cards ✓
7. Marcar a tarefa como concluída → tarefa passa para `APPROVED` diretamente (sem `PENDING_APPROVAL`) ✓
8. Saldo do filho é atualizado no extrato

**Fluxo Pai (retorno):**
9. Dashboard mostra a tarefa concluída na lista de pendentes para aprovação (ou já aprovada) ✓
10. Modal de revisão da tarefa não exibe seção de "Comprovante" ✓

**Fluxo de Cancelamento:**
11. Ir para Settings → modal de cancelamento de assinatura → lista de impactos não menciona Gift Cards ✓

### Verificação de Logs de Startup

Ao subir o backend, verificar no log de startup que **não aparecem** os seguintes erros:
```
ERROR - Failed to subscribe to [notification-subscription]
ERROR - Failed to connect to Pub/Sub topic [notification-topic]
BeanCreationException: Error creating bean with name 'notificationChannelAdapter'
```

## Critérios de Sucesso

- `./mvnw test` → BUILD SUCCESS, cobertura ≥ 60%
- `npm run build` → sem erros TypeScript
- `npm run test:e2e` → todos os testes restantes passam
- Fluxo manual completo do MVP validado sem referências às features removidas
- Nenhum erro de Pub/Sub de notificação nos logs de startup

## Testes da Tarefa

- [ ] **Backend**: `./mvnw clean test` com relatório JaCoCo
- [ ] **Frontend Build**: `npm run build`
- [ ] **E2E**: `npm run test:e2e` — foco nos fluxos de conclusão de tarefa e dashboard
- [ ] **E2E Manual**: roteiro de 11 passos descrito acima

<critical>SEMPRE CRIE E EXECUTE OS TESTES DA TAREFA ANTES DE CONSIDERÁ-LA FINALIZADA</critical>

## Arquivos Relevantes

- `backend/src/main/resources/application.yml` — **VERIFICAR** (sem propriedades obsoletas)
- `backend/src/main/resources/application-prod.yml` — **VERIFICAR/MODIFICAR** (se existir)
- Relatório JaCoCo: `backend/target/site/jacoco/index.html`
- Testes E2E: `frontend/tests/` ou `frontend/e2e/`
- [GCP Console — Pub/Sub](https://console.cloud.google.com/cloudpubsub/topic/list?project=gen-lang-client-0807030077) — **VERIFICAR** topics/subscriptions de notificação
- [GCP Console — Cloud Run Logs](https://console.cloud.google.com/logs/query?project=gen-lang-client-0807030077) — **VERIFICAR** logs de startup após deploy
