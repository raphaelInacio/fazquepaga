# Especificação Técnica: TaskAndPay (Baseline Dezembro 2025)

## Resumo Executivo

Este documento descreve o design técnico e o estado de implementação atual da funcionalidade TaskAndPay. O sistema é um **monólito modular** em **Java 17/Spring Boot 3.5.7**, implantado no **Google Cloud Run**, que se comunica com um frontend em React. A arquitetura está alinhada com o design original, utilizando **Google Cloud Firestore** para persistência e **Google Cloud Pub/Sub** (via Spring Cloud GCP 4.10.0) para eventos assíncronos, como a validação de imagens por IA via **Spring AI 1.1.0 (Google GenAI/Gemini)**.

Esta especificação foi atualizada para refletir a arquitetura "as-built", incluindo novos módulos, endpoints de API reais e desvios notáveis do plano original, servindo como uma baseline técnica precisa do projeto.

## Arquitetura do Sistema

### Organização de Domínio

A aplicação é um monólito com uma estrutura interna modular. O código está organizado por funcionalidade nos seguintes pacotes:

-   `identity/` - Gerenciamento de usuários (pais, filhos) e autenticação.
-   `tasks/` - Criação e gerenciamento de tarefas.
-   `allowance/` - Motor de cálculo de valores de mesada.
-   `ai/` - Lógica de negócio para interação com o Vertex AI (Gemini).
-   `whatsapp/` - Orquestra interações com a API do WhatsApp (Twilio), incluindo o webhook para conclusão de tarefas.
-   `security/` - **(Novo)** Rate limiting, reCAPTCHA, refresh tokens e hardening de autenticação.
-   `giftcard/` - **(Novo)** Lógica de negócios para a loja de Gift Cards (funcionalidade Premium).
-   `subscription/` - **(Novo)** Lógica de negócio que governa as permissões e limites dos planos Free vs. Premium.
-   `payment/` - **(Novo)** Integração com gateway Asaas para assinaturas (Customer, Subscription, Webhook).
-   `notification/` - **(Novo)** Hub de notificações (WhatsApp) para avisos de tarefas e saques.
-   `shared/` - Utilitários, configurações e tipos de dados compartilhados.

### Visão Geral dos Componentes

-   **Núcleo da Aplicação (Cloud Run)**: Implantação única que expõe uma API REST para o frontend e um webhook para o WhatsApp.
-   **Google Cloud Firestore**: Banco de dados NoSQL principal.
-   **Google Cloud Pub/Sub**: Usado para desacoplar tarefas assíncronas. O principal caso de uso é o evento `ProofSubmittedEvent`, que aciona a validação de imagens.
-   **Validador de IA (TaskProofListener)**: Um listener (`@Component`) dentro da aplicação principal que escuta mensagens do Pub/Sub, chama a API do Vertex AI e atualiza o status da tarefa no Firestore.

**Fluxo de Dados (Validação por IA):**
1.  A criança envia uma imagem via WhatsApp.
2.  O webhook no módulo `whatsapp` recebe a mensagem e publica um evento `ProofSubmittedEvent` no Pub/Sub.
3.  O `TaskProofListener` é acionado, chama o `AiValidatorImpl` que se conecta ao Vertex AI (Gemini Vision).
4.  O serviço atualiza o status da tarefa no Firestore e a sinaliza como `aiValidated`.
5.  **GAP:** O fluxo de notificação e aprovação final pelo pai no portal web não está implementado.

## Design da Implementação

### Modelos de Dados (Firestore)

A estrutura no Firestore segue o design original, com a adição de um campo para gerenciar assinaturas.

-   **Coleção:** `users`
    -   **Campos Adicionais:**
        -   `subscriptionTier`: STRING ('FREE' ou 'PREMIUM')
        -   `subscriptionStatus`: STRING ('ACTIVE', 'OVERDUE', 'CANCELED')
        -   `asaasCustomerId`: STRING
        -   `subscriptionId`: STRING (ID da assinatura no Asaas)
        -   `trialStartDate`: TIMESTAMP (Data de início do trial, UTC - setada automaticamente no registro)

-   **Coleção:** `children`
    -   **Campos Adicionais:**
        -   `aiContext`: STRING (Texto livre sobre interesses/bio da criança para a IA)
        -   `balance`: NUMBER (Saldo recalculado ou persistido para performance)

-   **Coleção:** `ledger`
    -   **Campos Adicionais:**
        -   `type`: STRING ('TASK_EARNING', 'WITHDRAWAL')
        -   `status`: STRING ('COMPLETED', 'PENDING_APPROVAL', 'PAID', 'REJECTED')
        -   `paymentProof`: STRING (Opcional - link ou código de comprovante manual)

### Data Models de Segurança (Firestore)

-   **Coleção:** `users/{userId}/quotas` (Subcoleção)
    -   **Document:** `ai`
        -   `usedToday`: NUMBER (Contador diário)
        -   `lastResetDate`: STRING (Data ISO do último reset)
        -   `dailyLimit`: NUMBER (Limite baseado no plano)

-   **Coleção:** `refreshTokens`
    -   **Document:** `{tokenId}`
        -   `userId`: STRING
        -   `tokenHash`: STRING (SHA-256)
        -   `expiresAt`: TIMESTAMP (30 dias)
        -   `revoked`: BOOLEAN

### Endpoints da API (Implementados)

A lista a seguir representa os endpoints que estão de fato implementados e sendo utilizados pelo frontend.

-   `POST /api/v1/auth/register` - Registra um novo pai.
-   `POST /api/v1/children` - Cria um perfil de filho.
-   `POST /api/v1/children/{childId}/onboarding-code` - Gera um código para a criança se registrar no WhatsApp.
-   `POST /api/v1/children/{childId}/allowance` - Define o valor da mesada total para a criança.
-   `GET /api/v1/tasks?child_id={uuid}` - Obtém as tarefas de uma criança.
-   `POST /api/v1/tasks?child_id={uuid}` - Cria uma nova tarefa para uma criança.
-   `GET /api/v1/allowance/predicted?child_id={uuid}` - Retorna o valor projetado da mesada com base nas tarefas existentes.
-   `GET /api/v1/ai/tasks/suggestions?age={age}` - Obtém sugestões de tarefas (Premium).
-   `GET /api/v1/giftcards` - Retorna uma lista mockada de gift cards (Premium).
-   `POST /api/v1/giftcards/{giftCardId}/redeem` - Simula o resgate de um gift card (Premium).
-   `POST /api/v1/whatsapp/webhook` - Webhook para receber eventos do WhatsApp (Twilio).
-   `POST /api/v1/tasks/{id}/approve` - Aprova uma tarefa.
-   `POST /api/v1/tasks/{id}/acknowledge` - Reconhece uma tarefa auto-aprovada.
-   `POST /api/v1/tasks/{id}/reject` - Rejeita uma tarefa.
-   `GET /api/v1/users/{id}` - Obtém detalhes de um usuário.
-   `GET /api/v1/children` - Obtém lista de filhos do pai autenticado.
-   `GET /api/v1/children/{id}` - Obtém detalhes de um filho.
-   **Novos Endpoints (Assinatura):**
    -   `POST /api/v1/subscription/subscribe` - Cria uma `Checkout Session` no Asaas e retorna a URL segura. **(Zero Data: Não recebe dados sensíveis do frontend)**.
    -   `GET /api/v1/subscription/status` - Consulta status atual da assinatura.
    -   `POST /api/v1/webhooks/asaas` - Recebe atualizações de pagamento do Asaas.
-   **Novos Endpoints (Saque):**
    -   `POST /api/v1/children/{childId}/withdraw` - Solicita saque (Criança).
    -   `POST /api/v1/withdrawals/{id}/approve` - Marca saque como pago (Pai).
-   **Novos Endpoints (AI Context):**
    -   `PATCH /api/v1/children/{childId}/context` - Atualiza bio/interesses.

### GAPs e Desvios da API

-   **Nenhum**: Os endpoints críticos de gerenciamento de dados e fluxo de aprovação foram implementados.

## Abordagem de Testes

-   **Testes Unitários**: A lógica de cálculo em `allowance` e as regras de negócio em `subscription` são os focos principais.
-   **Testes de Integração**: Testes validam o fluxo de criação de tarefas e o fluxo assíncrono com o emulador do Pub/Sub.

## Sequenciamento de Desenvolvimento (Status)

1.  **Concluído**: Módulos `identity` & `tasks`, `allowance`, `whatsapp`, `ai`, `giftcard`, e `subscription`.
2.  **Concluído**: Integração completa com o frontend incluindo Portal da Criança e Dashboard dos Pais.
3.  **Concluído**: Fluxos de Aprovação e Gestão de Dados (GETs) implementados.
4.  **Próximos Passos**: Monitoramento de custos de IA e refatoração para escalabilidade.

## Considerações Técnicas

### Decisões Chave (As-Built)

-   **Arquitetura**: Monólito Modular provou ser eficaz para a velocidade de desenvolvimento do MVP.
-   **Stack de Tecnologia**: A stack do Google Cloud (Cloud Run, Firestore, Pub/Sub, Vertex AI) está funcionando conforme o esperado.
-   **Interação da Criança**: A decisão de usar o WhatsApp como a interface primária da criança para o MVP simplificou o desenvolvimento, mas introduziu dependência em um único canal.
-   **Privacidade e Compliance**: Uso estrito de **Checkout Session** (Redirect) para pagamentos, garantindo que dados sensíveis (CPF, Cartão) nunca transitem pela infraestrutura do TaskAndPay.

### Riscos Conhecidos

-   **Dependência do Frontend em `localStorage`**: [MITIGADO] Endpoints GET foram implementados e o frontend está sendo migrado para consumir dados frescos.
-   **Custo da IA**: O custo do Gemini Vision para validação de imagens em escala é um risco financeiro. O monitoramento é essencial.

### Conformidade com Padrões

-   A arquitetura e a implementação seguem os padrões definidos e documentados em `AGENTS.MD`.