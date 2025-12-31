# Especificação Técnica: Cancelamento de Assinatura

## Resumo Executivo

Esta especificação detalha a implementação do fluxo de cancelamento self-service de assinaturas Premium no TaskAndPay. A solução adiciona um endpoint REST no módulo `subscription`, integra com a API Asaas via `DELETE /v3/subscriptions/{id}`, e utiliza a infraestrutura de notificação existente (Pub/Sub + Twilio) para confirmação via WhatsApp.

**Decisão arquitetural chave**: O usuário cancelado receberá status `PENDING_CANCELLATION` mas manterá tier `PREMIUM` até que o webhook Asaas confirme o fim efetivo da assinatura, evitando a necessidade de jobs agendados.

## Arquitetura do Sistema

### Organização de Domínio

Os componentes pertencem aos seguintes pacotes existentes:

| Pacote | Responsabilidade |
|--------|------------------|
| `subscription/` | Controller, Service e DTOs do cancelamento |
| `payment/` | `AsaasService` para chamada à API externa |
| `notification/` | Envio de notificação via Pub/Sub |
| `identity/` | Entity `User` com novos campos |

### Visão Geral dos Componentes

```
┌─────────────────────────────────────────────────────────────────────┐
│                           Frontend                                   │
│  ┌─────────────────┐    ┌──────────────────────────────────────┐   │
│  │   SettingsPage  │───►│  CancelSubscriptionModal (NEW)       │   │
│  └─────────────────┘    │  - Step 1: ChurnSurvey               │   │
│                         │  - Step 2: ConfirmationWithImpact    │   │
│                         └──────────────────┬───────────────────┘   │
└────────────────────────────────────────────┼───────────────────────┘
                                             │ POST /api/v1/subscription/cancel
                                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                           Backend                                    │
│  ┌──────────────────────┐    ┌─────────────────────────────────┐   │
│  │ SubscriptionController│───►│ SubscriptionService             │   │
│  │ POST /cancel          │    │ - cancelSubscription()          │   │
│  └──────────────────────┘    └──────────────┬──────────────────┘   │
│                                              │                       │
│                    ┌─────────────────────────┼──────────────────┐   │
│                    ▼                         ▼                  ▼   │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌───────────────┐│
│  │ AsaasService        │  │ UserRepository      │  │ Notification  ││
│  │ - cancelSubscription│  │ - save(user)        │  │ Service       ││
│  └──────────┬──────────┘  └─────────────────────┘  └───────┬───────┘│
└─────────────┼──────────────────────────────────────────────┼───────┘
              │                                               │
              ▼                                               ▼
    ┌─────────────────┐                              ┌───────────────┐
    │   Asaas API     │                              │  Pub/Sub      │
    │ DELETE /subs/{id}│                             │  → WhatsApp   │
    └─────────────────┘                              └───────────────┘
```

## Design da Implementação

### Interfaces Principais

```java
// SubscriptionService.java - Novo método
public CancelSubscriptionResponse cancelSubscription(String userId, CancellationReason reason);

// AsaasService.java - Novo método
public boolean cancelSubscription(String subscriptionId);

// NotificationService.java - Novo método
public void sendSubscriptionCanceled(User user, Instant premiumExpirationDate);
```

### Modelos de Dados

#### Novo Enum: CancellationReason

```java
// subscription/CancellationReason.java
public enum CancellationReason {
    TOO_EXPENSIVE,
    NOT_USING_FEATURES,
    FOUND_ALTERNATIVE,
    WILL_RETURN_LATER,
    OTHER
}
```

#### Novo Status: PENDING_CANCELLATION

```java
// identity/User.java - Modificar enum existente
public enum SubscriptionStatus {
    ACTIVE,
    CANCELED,
    PAST_DUE,
    PENDING_CANCELLATION  // NOVO
}
```

#### Novos Campos em User

```java
// identity/User.java - Adicionar campos
private Instant cancellationDate;
private CancellationReason cancellationReason;
private String cancellationReasonDetails; // Para "OTHER"
```

#### Request DTO

```java
// subscription/dto/CancelSubscriptionRequest.java
@Data
@Builder
public class CancelSubscriptionRequest {
    @NotNull
    private CancellationReason reason;
    
    private String details; // Opcional, usado quando reason = OTHER
}
```

#### Response DTO

```java
// subscription/dto/CancelSubscriptionResponse.java
@Data
@Builder
public class CancelSubscriptionResponse {
    private boolean success;
    private Instant premiumExpiresAt; // Data até quando Premium será mantido
    private String message;
}
```

### Endpoints da API

| Método | Path | Descrição | Auth |
|--------|------|-----------|------|
| `POST` | `/api/v1/subscription/cancel` | Cancela assinatura do usuário autenticado | Bearer Token (Parent) |

**Request Body:**
```json
{
    "reason": "TOO_EXPENSIVE",
    "details": null
}
```

**Response (200 OK):**
```json
{
    "success": true,
    "premiumExpiresAt": "2025-01-30T00:00:00Z",
    "message": "Assinatura cancelada. Acesso Premium até 30/01/2025."
}
```

**Erros:**
| Status | Condição |
|--------|----------|
| `400` | Usuário não tem assinatura ativa |
| `403` | Usuário não é PARENT |
| `500` | Falha na API Asaas |

## Pontos de Integração

### API Asaas

**Endpoint:** `DELETE /v3/subscriptions/{id}`

**Headers:**
```
access_token: ${asaas.api-key}
```

**Resposta esperada:**
```json
{
    "deleted": true,
    "id": "sub_VXJBYgP2u0eO"
}
```

**Tratamento de erros:**
- `404`: Assinatura não encontrada → Log warning, prosseguir com status local
- `400`/`500`: Falha na API → Lançar exceção, não alterar status local

### Webhook Asaas

O `AsaasWebhookController` existente já trata eventos de desativação. Adicionar tratamento para evento `SUBSCRIPTION_DELETED` (se Asaas enviar) para confirmar e rebaixar tier:

```java
// AsaasWebhookController.java - Adicionar case
case SUBSCRIPTION_DELETED:
    subscriptionService.confirmCancellation(asaasCustomerId);
    break;
```

## Análise de Impacto

| Componente Afetado | Tipo de Impacto | Descrição | Ação |
|--------------------|-----------------|-----------|------|
| `User.java` | Schema Change | 3 novos campos, 1 novo enum value | Firestore aceita novas fields automaticamente |
| `SubscriptionController` | API Addition | Novo endpoint `/cancel` | Nenhuma breaking change |
| `SubscriptionService` | Logic Addition | Novo método | Nenhuma breaking change |
| `AsaasService` | Logic Addition | Novo método | Nenhuma breaking change |
| `NotificationService` | Logic Addition | Novo método para cancelamento | Adicionar `SUBSCRIPTION_CANCELED` ao enum |
| `NotificationType` | Enum Addition | Novo valor | Nenhuma breaking change |
| Frontend `SettingsPage` | UI Addition | Novo botão | Nenhuma breaking change |

## Abordagem de Testes

### Testes Unitários

**SubscriptionServiceTest:**
- `cancelSubscription_success` - Happy path
- `cancelSubscription_noActiveSubscription_throws` - Usuário sem assinatura
- `cancelSubscription_asaasFails_throws` - Falha na API externa

**AsaasServiceTest:**
- `cancelSubscription_success` - Mock RestTemplate retorna sucesso
- `cancelSubscription_notFound_returnsTrue` - 404 tratado como sucesso

### Testes de Integração

- `POST /api/v1/subscription/cancel` - E2E com mock do Asaas
- Verificar que User foi atualizado com `PENDING_CANCELLATION`
- Verificar que notificação foi publicada no Pub/Sub

## Sequenciamento de Desenvolvimento

### Ordem de Build

1. **Backend Core** (1-2h)
   - `CancellationReason` enum
   - `User.SubscriptionStatus.PENDING_CANCELLATION`
   - Campos em `User.java`
   - DTOs de request/response

2. **AsaasService** (1h)
   - Método `cancelSubscription()`
   - Tratamento de erros

3. **SubscriptionService** (1-2h)
   - Método `cancelSubscription()`
   - Integração com AsaasService e NotificationService

4. **NotificationService** (30min)
   - `SUBSCRIPTION_CANCELED` no enum
   - Método `sendSubscriptionCanceled()`

5. **SubscriptionController** (30min)
   - Endpoint `POST /cancel`

6. **Testes Backend** (1-2h)
   - Unit tests
   - Integration tests

7. **Frontend** (2-3h)
   - `CancelSubscriptionModal` componente
   - Integração com `SettingsPage`
   - Service method

### Dependências Técnicas

- RestTemplate configurado para Asaas (`asaasRestTemplate`) ✓ Existente
- Pub/Sub topic configurado ✓ Existente
- Template WhatsApp para cancelamento → **Necessário criar/aprovar**

## Monitoramento e Observabilidade

### Logs

| Nível | Evento | Campos |
|-------|--------|--------|
| `INFO` | Cancelamento iniciado | userId, reason |
| `INFO` | Asaas API chamada | subscriptionId |
| `WARN` | Asaas 404 | subscriptionId |
| `ERROR` | Asaas falha | subscriptionId, error |
| `INFO` | Cancelamento concluído | userId, premiumExpiresAt |

### Métricas (Futuro)

- `subscription.cancellations.total` - Counter por reason
- `subscription.cancellations.asaas.errors` - Counter de falhas

## Considerações Técnicas

### Decisões Chave

| Decisão | Rationale |
|---------|-----------|
| Status `PENDING_CANCELLATION` | Diferencia do `CANCELED` por inadimplência, permite fluxo controlado |
| Confiar em webhooks | Evita jobs agendados e complexidade, Asaas é responsável por notificar fim |
| Enum para reasons | Type-safety, facilita analytics futura, previne dados inconsistentes |
| Manter Premium até fim | Boa prática SaaS, usuário pagou pelo período corrente |

### Riscos Conhecidos

| Risco | Mitigação |
|-------|-----------|
| Webhook Asaas não chegar | Monitorar usuários em `PENDING_CANCELLATION` por mais de 30 dias |
| Template WhatsApp não aprovado | Preparar fallback simples ou omitir notificação inicialmente |

### Conformidade com Padrões

- ✅ Segue `use-java-spring-boot.mdc` - Spring Boot 3.x patterns
- ✅ Segue `api-rest-http.mdc` - RESTful conventions
- ✅ Segue `firestore-nosql.mdc` - Campos adicionados são compatíveis
- ✅ Segue `tests.mdc` - Unit + Integration tests planejados
- ✅ Segue `logging.mdc` - Logs estruturados com campos relevantes
