# Guia de Integração Asaas (TaskAndPay)

Este documento detalha como implementar a integração com o gateway de pagamentos Asaas para a funcionalidade de assinatura Premium.

## 1. Visão Geral do Fluxo

1.  **Cadastro do Cliente (`Customer`)**: Todo pai/mãe deve ter um `customerId` no Asaas.
2.  **Criação da Assinatura (`Subscription`)**: Gera uma cobrança recorrente (ciclo mensal).
3.  **Pagamento**: O usuário é redirecionado para o Link de Pagamento do Asaas ou preenche os dados (se transparente).
4.  **Confirmação (Webhook)**: O backend recebe um `PAYMENT_RECEIVED` e libera o acesso Premium.

## 2. Endpoints & Schemas

## 2. Endpoints & Schemas

### 2.1. Criar Sessão de Checkout (`POST /v3/checkouts`)

Para evitar armazenar dados sensíveis (CPF, Cartão), utilizaremos o **Checkout Session**. O usuário é redirecionado para uma página segura do Asaas onde preenche seus dados.

**Request:**

```json
{
  "chargeTypes": ["RECURRENT"],
  "billingTypes": ["CREDIT_CARD", "PIX", "BOLETO"],
  "items": [
    {
      "name": "TaskAndPay Premium",
      "value": 29.90
    }
  ],
  "subscription": {
    "cycle": "MONTHLY",
    "description": "Assinatura Mensal TaskAndPay"
  },
  "callback": {
    "successUrl": "https://taskandpay.com/app/settings?success=true",
    "cancelUrl": "https://taskandpay.com/app/settings?cancel=true"
  },
  "externalReference": "USER_UUID_FROM_DB",
  "notificationEnabled": true
}
```

**Response (Sucesso):**

```json
{
  "id": "chk_000005167664",
  "checkoutUrl": "https://www.asaas.com/c/54564654",
  ...
}
```

*   **Ação**: Redirecionar o usuário para `checkoutUrl`.

### 2.2. Webhooks (`POST /v3/webhooks`)

A URL do webhook deve ser: `https://api.taskandpay.com/api/v1/webhooks/asaas`.

**Eventos Principais:**

*   `PAYMENT_RECEIVED`: Pagamento da assinatura confirmado.
*   `SUBSCRIPTION_CREATED`: Assinatura criada com sucesso (após o checkout).

**Payload do Webhook (Exemplo):**

```json
{
  "event": "PAYMENT_RECEIVED",
  "payment": {
    "customer": "cus_000005167664",
    "subscription": "sub_kjdk39483",
    "externalReference": "USER_UUID_FROM_DB", 
    ...
  }
}
```

*   **Nota**: O `externalReference` definido no Checkout é repassado para a Assinatura/Pagamento, permitindo identificar o usuário no webhook.

## 3. Estratégia de Implementação (Java/Spring)

1.  **Dependência**: Usar `RestTemplate` ou `WebClient`.
2.  **Segurança**: Não salvar CPF/Cartão no banco. Salvar apenas `asaasCustomerId` e `subscriptionId` recebidos via Webhook.

### Mapeamento de Classes

*   `AsaasService`:
    *   `createCheckoutSession(User user)` -> retorna URL.
*   `AsaasWebhookController`:
    *   Processa `PAYMENT_RECEIVED` para ativar Premium e salvar IDs do Asaas.
