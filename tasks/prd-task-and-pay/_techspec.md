# Especificação Técnica: TaskAndPay (Baseline Novembro 2025)

## Resumo Executivo

Este documento descreve o design técnico e o estado de implementação atual da funcionalidade TaskAndPay. O sistema é um **monólito modular** em Java/Spring Boot, implantado no **Google Cloud Run**, que se comunica com um frontend em React. A arquitetura está alinhada com o design original, utilizando **Google Cloud Firestore** para persistência e **Google Cloud Pub/Sub** para eventos assíncronos, como a validação de imagens por IA via **Google Vertex AI (Gemini)**.

Esta especificação foi atualizada para refletir a arquitetura "as-built", incluindo novos módulos, endpoints de API reais e desvios notáveis do plano original, servindo como uma baseline técnica precisa do projeto.

## Arquitetura do Sistema

### Organização de Domínio

A aplicação é um monólito com uma estrutura interna modular. O código está organizado por funcionalidade nos seguintes pacotes:

-   `identity/` - Gerenciamento de usuários (pais, filhos) e autenticação.
-   `tasks/` - Criação e gerenciamento de tarefas.
-   `allowance/` - Motor de cálculo de valores de mesada.
-   `ai/` - Lógica de negócio para interação com o Vertex AI (Gemini).
-   `whatsapp/` - Orquestra interações com a API do WhatsApp (Twilio), incluindo o webhook para conclusão de tarefas.
-   `giftcard/` - **(Novo)** Lógica de negócios para a loja de Gift Cards (funcionalidade Premium).
-   `subscription/` - **(Novo)** Lógica de negócio que governa as permissões e limites dos planos Free vs. Premium.
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

### GAPs e Desvios da API

-   **Endpoint Ausente**: `POST /api/v1/tasks/{id}/approve`. A aprovação de tarefas pelo pai no portal web não foi implementada.
-   **Endpoint Ausente**: Não existem endpoints `GET` para recuperar entidades individuais, como `GET /api/v1/children/{id}`. O frontend contorna essa limitação utilizando `localStorage`, uma solução frágil.

## Abordagem de Testes

-   **Testes Unitários**: A lógica de cálculo em `allowance` e as regras de negócio em `subscription` são os focos principais.
-   **Testes de Integração**: Testes validam o fluxo de criação de tarefas e o fluxo assíncrono com o emulador do Pub/Sub.

## Sequenciamento de Desenvolvimento (Status)

1.  **Concluído**: Módulos `identity` & `tasks` (com os GAPs mencionados), `allowance`, `whatsapp`, `ai`, `giftcard`, e `subscription`.
2.  **Concluído**: Integração básica com o frontend para as funcionalidades existentes.
3.  **Próximos Passos (Críticos)**:
    -   Implementar o endpoint `POST /api/v1/tasks/{id}/approve`.
    -   Desenvolver a UI no frontend para o pai visualizar provas e aprovar tarefas.
    -   Implementar endpoints `GET` para entidades (`children`, `users`) para remover a dependência do `localStorage` no frontend.

## Considerações Técnicas

### Decisões Chave (As-Built)

-   **Arquitetura**: Monólito Modular provou ser eficaz para a velocidade de desenvolvimento do MVP.
-   **Stack de Tecnologia**: A stack do Google Cloud (Cloud Run, Firestore, Pub/Sub, Vertex AI) está funcionando conforme o esperado.
-   **Interação da Criança**: A decisão de usar o WhatsApp como a interface primária da criança para o MVP simplificou o desenvolvimento, mas introduziu dependência em um único canal.

### Riscos Conhecidos

-   **Dependência do Frontend em `localStorage`**: O frontend depende do `localStorage` para compartilhar estado (ex: dados da criança) entre as páginas. Isso é frágil e pode levar a bugs de inconsistência de dados. A falta de endpoints `GET` para buscar dados frescos é a causa raiz e representa um débito técnico.
-   **Fluxo de Aprovação Incompleto**: O fluxo de negócio mais importante (aprovação de tarefas) está quebrado. A criança pode submeter, mas o pai não pode aprovar via web, impedindo a conclusão do ciclo de valor.
-   **Custo da IA**: O custo do Gemini Vision para validação de imagens em escala é um risco financeiro. O monitoramento é essencial.

### Conformidade com Padrões

-   A arquitetura e a implementação seguem em grande parte os padrões definidos, com os desvios e GAPs agora documentados nesta especificação.