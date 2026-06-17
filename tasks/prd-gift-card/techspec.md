# Especificação Técnica: Integração de Gift Cards via RV Hub

## Resumo Executivo

Esta especificação define a implementação técnica para a integração de Gift Cards no projeto TaskAndPay, utilizando as APIs da RV Hub (Recarga de PIN) e o gateway Asaas para cobrança. O fluxo garante a segurança financeira exigindo aprovação manual dos pais: ao aprovar, o sistema debita do cartão de crédito do pai via Asaas de forma síncrona, faz a chamada de emissão (RV Hub) com *Idempotency Key*, e apenas com sucesso consolida a transação e libera o voucher para o dependente. 

## Arquitetura do Sistema

### Visão Geral dos Componentes

- **`GiftCardController`**: Exporá novos endpoints para que o dependente solicite a recarga e para que o pai a aprove.
- **`GiftCardService`**: Orquestrará a lógica de negócios, unindo o gateway de pagamento (Asaas), a API de emissão (RV Hub) e a persistência (Firestore).
- **`RVHubClient` (NOVO)**: Componente cliente REST responsável por interagir com o RV Hub (Autenticação JWT, Solicitação de Recarga e Confirmação).
- **`AsaasService` (EXISTENTE)**: Serviço já utilizado para cobrança; será responsável por gerar uma cobrança (*Charge*) utilizando o cartão de crédito tokenizado salvo no perfil do pai.
- **`GiftCardTransactionRepository` (NOVO)**: Repositório Firestore conectado à coleção raiz `giftcard_transactions`.

## Design de Implementação

### Interfaces Principais

```java
public interface RVHubClient {
    // Autentica via client_credentials e retorna o token Bearer JWT
    String authenticate();
    
    // Solicita a recarga (Passo 1 do RV Hub) com Idempotency Key
    RVHubTransactionResponse requestPinTopup(String productId, BigDecimal amount, String idempotencyKey);
    
    // Captura a recarga (Passo 2 do RV Hub)
    RVHubCaptureResponse capturePinTopup(String transactionId);
}

public interface GiftCardService {
    // Dependente adiciona solicitação
    GiftCardTransaction requestGiftCard(String childId, String parentId, String productId, BigDecimal amount);
    
    // Pai aprova
    GiftCardTransaction approveGiftCard(String parentId, String transactionId);
}
```

### Modelos de Dados

- **`GiftCardTransaction` (Firestore Entity)**:
  - `id` (String)
  - `childId` (String)
  - `parentId` (String)
  - `productId` (String) - ID do RVHub
  - `amount` (BigDecimal) - Valor fiduciário
  - `status` (Enum: `PENDING`, `APPROVED`, `FAILED`, `COMPLETED`)
  - `asaasPaymentId` (String)
  - `rvhubTransactionId` (String)
  - `pinCode` (String) - Voucher gerado
  - `createdAt` (Timestamp)
  - `idempotencyKey` (String) - UUID gerado no momento da solicitação

### Endpoints de API

- `GET /api/v1/giftcards/catalog`: Retorna o catálogo filtrado (mock atual será substituído).
- `POST /api/v1/giftcards/requests`: Dependente solicita um gift card. (Retorna 201 Created).
- `GET /api/v1/giftcards/requests`: Dependente ou Pai lista suas solicitações pendentes/concluídas.
- `POST /api/v1/giftcards/requests/{id}/approve`: Pai aprova o request. Dispara a cobrança síncrona.

## Pontos de Integração

1. **Gateway Asaas**: Cobrança utilizando `CreditCard`. Em caso de recusa (saldo insuficiente no cartão, etc.), a transação do RVHub não é disparada e o status fica `FAILED`.
2. **RV Hub**: Integração via HTTPS. Utiliza-se a abordagem idempotente em `POST /pin-topups/transactions` passando `X-Idempotency-Key` para prevenir a dupla emissão caso ocorra *timeout* entre o nosso Backend e o RV Hub.

## Abordagem de Testes

### Testes Unidade
- **`GiftCardServiceTest`**: Validar se o fluxo transacional cancela/estorna o pagamento do Asaas caso a chamada à API do RV Hub retorne erro interno (5xx). Mockar tanto `AsaasService` quanto `RVHubClient`.

### Testes E2E
- Implementar fluxos em **Playwright** navegando com usuário Dependente (solicitando PIN) e em seguida com usuário Pai (aprovando). A resposta das chamadas REST será interceptada para retornar PINs de teste simulando a Sandbox.

## Sequenciamento de Desenvolvimento

### Ordem de Construção

1. **Camada de Integração**: Desenvolver `RVHubClient` e DTOs, validando a conexão com o ambiente Sandbox deles.
2. **Camada de Dados**: Criar o modelo Firestore `GiftCardTransaction` e seus repositórios.
3. **Casos de Uso**: Implementar o fluxo no `GiftCardService` que interliga Asaas + RVHub + Firestore com tratamento de erros.
4. **Controllers**: Expor APIs REST (substituindo os mocks de `GiftCardController`).
5. **Frontend**: Adaptar a interface React (adição da feature "Carrinho/Aprovação").

### Dependências Técnicas
- Obtenção das chaves (Client ID e Secret) do Portal do Desenvolvedor RV Hub e injeção via `SecretManager`/`application.yml`.

## Monitoramento e Observabilidade
- Registrar no SLF4J (GCP Cloud Logging) falhas na emissão de PIN com tag `[GIFT_CARD_ERROR]` contendo o `asaasPaymentId`, a fim de agilizar o estorno manual em caso de falha irreversível não coberta pelo código.

## Considerações Técnicas

### Decisões Principais
- **Fluxo Síncrono no Pai**: Decidimos que o botão "Aprovar" executará o processo Asaas -> RVHub de forma síncrona (Aguardando resposta REST). Isso evita cenários onde o pai aprova e o cartão passa horas depois, ou o voucher falha após um período longo.
- **Idempotência**: Uma chave de idempotência é gerada no momento que o dependente cria a requisição. A mesma chave será usada na API do RVHub.

### Riscos Conhecidos
- **Cobrado mas não emitido**: Risco do Asaas confirmar a cobrança, mas a API do RVHub ficar fora do ar por tempo indeterminado.
- **Mitigação**: Um bloco `try-catch` capturará o erro e fará a requisição imediata de *Refund* (estorno) para o `AsaasService`, falhando a transação e mantendo as finanças do pai intactas.

### Conformidade com Skills Padrões
- `api-rest-http.md`: Uso consistente de métodos HTTP (POST para approve) e status codes (201 para novos requests).
- `asaas-integration.md`: Reutilização da arquitetura de integração Asaas já estabelecida no projeto.
- `firestore-nosql.md`: Escolha de Coleção Raiz para facilitar buscas indexadas e escalabilidade, evitando subcoleções aninhadas demais.

### Arquivos relevantes e dependentes
- `backend/src/main/java/com/fazquepaga/taskandpay/giftcard/GiftCardController.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/payment/AsaasService.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/giftcard/RVHubClient.java` (A Criar)
