# Especificação Técnica: TaskAndPay

## Resumo Executivo

Este documento descreve o design técnico para a funcionalidade TaskAndPay, uma plataforma para gerenciar tarefas e mesadas de crianças. O sistema será implementado como um **monólito modular** implantado no **Google Cloud Run**. Essa abordagem equilibra a velocidade de desenvolvimento com uma estrutura limpa e orientada a funcionalidades, evitando a complexidade inicial de uma arquitetura de microsserviços.

O backend será uma única aplicação organizada em módulos lógicos (`identity`, `tasks`, `allowance`, `ai`, `whatsapp`). Ele usará o **Google Cloud Firestore** para armazenamento de dados e o **Google Cloud Pub/Sub** para lidar com operações assíncronas, como a validação de imagens por IA. As capacidades de IA (sugestões de tarefas, análise de imagens) serão fornecidas pelo **modelo Gemini do Google via Vertex AI**. Todo o sistema será monitorado usando a suíte de operações do Google Cloud.

## Arquitetura do Sistema

### Organização de Domínio

A aplicação será um monólito com uma estrutura interna modular. O código será organizado por funcionalidade nos seguintes pacotes de nível superior:

-   `identity/` - Gerenciamento de usuários (pais, filhos), autenticação e segurança de perfis.
-   `tasks/` - Criação, gerenciamento e transições de estado de tarefas (ex: pendente, concluída, aprovada).
-   `allowance/` - O motor principal para calcular os valores das tarefas com base na mesada mensal e nos pesos.
-   `ai/` - Cliente e lógica de negócios para interagir com o Vertex AI (Gemini) para sugestões de tarefas e validação de imagens.
-   `whatsapp/` - Lida com todas as interações com a API do WhatsApp Business, incluindo o webhook para mensagens recebidas e o fluxo de onboarding da criança.
-   `shared/` - Utilitários principais, tipos de dados e interfaces compartilhadas entre os módulos.

### Visão Geral dos Componentes

-   **Núcleo da Aplicação (Cloud Run)**: Uma única unidade implantável contendo todos os módulos listados acima. Expõe uma API REST para o frontend web dos pais e um webhook para o provedor do WhatsApp.
-   **Google Cloud Firestore**: O banco de dados de documentos NoSQL principal que armazena todos os dados da aplicação, incluindo perfis de usuário, tarefas e informações de registro financeiro em uma estrutura baseada em coleções.
-   **Google Cloud Pub/Sub**: Um barramento de mensagens usado para desacoplar o fluxo principal da aplicação de tarefas assíncronas e de longa duração. O principal caso de uso é a validação de imagens por IA.
-   **Validador de IA (Cloud Function/Run)**: Um serviço separado, orientado a eventos, acionado por uma mensagem do Pub/Sub. Ele recupera a imagem, chama a API do Vertex AI para validá-la e atualiza o status da tarefa adequadamente.

**Fluxo de Dados (Validação por IA):**
1.  Uma criança envia uma imagem para o número do WhatsApp.
2.  O webhook do módulo `whatsapp` recebe a mensagem e publica um evento `ProofSubmitted` em um tópico do Pub/Sub. A carga útil do evento contém o ID da criança e a referência da imagem.
3.  O serviço `AI Validator`, inscrito no tópico, é acionado.
4.  O serviço chama a lógica do módulo `ai`, que faz a interface com o Vertex AI (Gemini Vision) para analisar a imagem.
5.  O serviço atualiza o status da tarefa correspondente no banco de dados Firestore para `PENDING_APPROVAL` e a sinaliza como "verificada por IA".
6.  Uma notificação é enfileirada para o pai.

## Design da Implementação

### Interfaces Principais

```java
// Localizado em shared/
public interface TaskRepository {
    ApiFuture<WriteResult> save(Task task);
    ApiFuture<DocumentSnapshot> findById(String childId, String taskId);
    ApiFuture<QuerySnapshot> findTasksByChildId(String childId);
}

// Localizado em allowance/
public interface AllowanceCalculator {
    BigDecimal calculateTaskValue(Task task, BigDecimal monthlyAllowance, List<Task> allTasksForMonth);
}

// Localizado em ai/
public interface AiValidator {
    boolean validateTaskCompletionImage(byte[] image, String taskDescription);
}

// Localizado em whatsapp/
public interface WhatsAppClient {
    void sendMessage(String to, String message);
}
```

### Modelos de Dados

**Modelo de Dados (Firestore)**

Os dados serão armazenados no Google Cloud Firestore usando um modelo baseado em coleções e documentos.

-   **Coleção Raiz:** `users`
    -   **ID do Documento:** `userId` (ex: uma string UUID única)
    -   **Campos:**
        -   `name`: STRING
        -   `email`: STRING (para pais)
        -   `role`: STRING ('PARENT' ou 'CHILD')
        -   `parentId`: STRING (para crianças, ligando ao `userId` do pai)
        -   `phoneNumber`: STRING (para crianças, usado para identificação no WhatsApp)
    -   **Subcoleção:** `tasks` (existe sob os documentos de usuário da criança)
        -   **ID do Documento:** `taskId`
        -   **Campos:**
            -   `description`: STRING
            -   `type`: STRING ('DAILY', 'WEEKLY', 'ONE_TIME')
            -   `weight`: STRING ('LOW', 'MEDIUM', 'HIGH')
            -   `status`: STRING ('PENDING', 'COMPLETED', 'APPROVED')
            -   `requiresProof`: BOOLEAN
            -   `createdAt`: TIMESTAMP
    -   **Subcoleção:** `task_completions` (existe sob os documentos de usuário da criança)
        -   **ID do Documento:** `completionId`
        -   **Campos:**
            -   `taskId`: STRING
            -   `completedAt`: TIMESTAMP
            -   `proofImageUrl`: STRING
            -   `aiValidated`: BOOLEAN
            -   `approvedAt`: TIMESTAMP
    -   **Subcoleção:** `ledger` (existe sob os documentos de usuário da criança)
        -   **ID do Documento:** `ledgerEntryId`
        -   **Campos:**
            -   `taskCompletionId`: STRING
            -   `amount`: NUMBER
            -   `transactionType`: STRING ('CREDIT')
            -   `createdAt`: TIMESTAMP

### Endpoints da API

-   `POST /api/v1/auth/register` - Registra um novo pai/mãe.
-   `POST /api/v1/children` - Cria um novo perfil de criança sob um pai/mãe.
-   `POST /api/v1/tasks` - Cria uma nova tarefa para uma criança.
-   `GET /api/v1/tasks?child_id={uuid}` - Obtém todas as tarefas de uma criança específica.
-   `POST /api/v1/tasks/{id}/approve` - Pai/mãe aprova uma tarefa concluída.
-   `POST /api/v1/whatsapp/webhook` - Webhook para receber mensagens do provedor do WhatsApp.

## Pontos de Integração

-   **Google Vertex AI (Gemini)**:
    -   A autenticação será tratada via credenciais de conta de serviço do GCP.
    -   O módulo `ai` será responsável por construir os prompts corretos para sugestões de tarefas e validação de imagens.
    -   O tratamento de erros incluirá novas tentativas com backoff exponencial para problemas transitórios de rede.
-   **Provedor da API do WhatsApp Business (ex: Twilio)**:
    -   A autenticação usará chaves/tokens de API fornecidos pelo fornecedor.
    -   O módulo `whatsapp` encapsulará a lógica para enviar mensagens e validar as assinaturas de webhooks recebidos.

## Análise de Impacto

Esta é uma nova funcionalidade, então o impacto é primariamente a criação de novos componentes. Nenhum componente existente é afetado.

| Componente Afetado          | Tipo de Impacto            | Descrição & Nível de Risco             | Ação Necessária      |
| --------------------------- | -------------------------- | -------------------------------------- | -------------------- |
| N/A (Nova Funcionalidade)   | N/A                        | N/A                                    | N/A                  |

## Abordagem de Testes

### Testes Unitários

-   **Módulo `allowance`**: A lógica do `AllowanceCalculator` deve ser exaustivamente testada com vários cenários, incluindo diferentes durações de meses, pesos de tarefas e casos extremos (ex: sem tarefas, mesada zero).
-   **Módulo `tasks`**: Testar as transições de estado e as regras de negócio para o gerenciamento de tarefas.
-   **Módulo `ai`**: Mockar o cliente do Vertex AI para testar a construção de prompts e a lógica de análise de respostas.
-   **Módulo `whatsapp`**: Mockar o cliente do WhatsApp para testar o fluxo de onboarding e a lógica de manipulação de mensagens.

### Testes de Integração

-   Um teste de integração será criado para validar o fluxo desde o endpoint da API (`POST /tasks`) até o banco de dados, garantindo que uma tarefa seja persistida corretamente.
-   Um segundo teste de integração cobrirá o fluxo assíncrono: publicando uma mensagem no emulador do Pub/Sub e verificando que a lógica do manipulador atualiza o banco de dados corretamente.

## Sequenciamento de Desenvolvimento

### Ordem de Construção

1.  **Módulos `identity` & `tasks`**: Implementar os modelos de dados e repositórios principais para usuários e tarefas. Configurar os endpoints básicos da API REST.
2.  **Módulo `allowance`**: Desenvolver e testar o motor principal de cálculo da mesada.
3.  **Módulo `whatsapp`**: Implementar o fluxo de onboarding da criança e o webhook para receber mensagens.
4.  **Módulo `ai` & Validador**: Implementar o cliente do Vertex AI e a lógica de validação assíncrona usando Pub/Sub.
5.  **Integração com Frontend**: Conectar a aplicação web dos pais à API do backend.

### Dependências Técnicas

-   Acesso a um projeto do Google Cloud Platform com faturamento ativado.
-   Credenciais para Vertex AI, Firestore e Pub/Sub.
-   Uma conta com um provedor de API do WhatsApp Business.

## Monitoramento & Observabilidade

-   **Métricas**: Usaremos o Google Cloud Monitoring para rastrear:
    -   Cloud Run: Latência de requisições, contagem de requisições e utilização de CPU/memória do contêiner.
    -   Pub/Sub: Número de mensagens não entregues e idade da subscrição.
    -   Firestore: Leituras, escritas e exclusões de documentos por segundo.
-   **Logging**: A aplicação usará logging estruturado em JSON direcionado para o **Google Cloud Logging**. Os logs incluirão um `trace_id` para correlacionar requisições através da aplicação e dos workers assíncronos.
-   **Alertas**: Alertas serão configurados no Cloud Monitoring para altas taxas de erro da API (>5%), alta latência de requisições e um número crescente de mensagens não entregues no Pub/Sub.

## Considerações Técnicas

### Decisões Chave

-   **Arquitetura**: Escolheu-se um **Monólito Modular** em vez de microsserviços para reduzir a sobrecarga operacional inicial, mantendo uma estrutura limpa e escalável.
-   **Stack de Tecnologia**: Selecionou-se a stack da **Google Cloud Platform** (Cloud Run, Firestore, Pub/Sub, Vertex AI) para alinhar com a preferência do usuário. O Firestore foi escolhido em vez do Cloud SQL por seu modelo de dados flexível e preço de custo-benefício em escala, o que se alinha bem com uma aplicação serverless e orientada a eventos.
-   **Processamento Assíncrono**: O uso do **Pub/Sub** para validação por IA evita o bloqueio da thread principal de requisição, garantindo que o webhook do `whatsapp` possa responder rapidamente ao provedor, o que é crítico para a confiabilidade.

### Riscos Conhecidos

-   **Complexidade do Onboarding do WhatsApp**: O processo para uma criança enviar um código via WhatsApp adiciona atrito. As instruções fornecidas pelos pais devem ser excepcionalmente claras.
-   **Custo da IA**: O custo de usar o modelo Gemini Vision para cada conclusão de tarefa visual pode ser significativo. Devemos monitorar isso de perto e, potencialmente, introduzir otimizações mais tarde (ex: validações por amostragem).
-   **Engenharia de Prompt**: A precisão da validação de imagem por IA é altamente dependente de uma engenharia de prompt eficaz. Isso exigirá iteração e testes para acertar.

### Conformidade com Padrões

-   A arquitetura segue os princípios de um sistema modular, organizado por funcionalidade.
-   A implementação seguirá as melhores práticas de Java/Spring Boot.
-   Todas as APIs REST serão projetadas seguindo as convenções padrão.
-   Os testes seguirão a estratégia delineada neste documento.

## Ambiente de Desenvolvimento

Para garantir consistência e facilitar a configuração, o ambiente de desenvolvimento será totalmente containerizado usando Docker e Docker Compose. Isso nos permite replicar o ambiente de produção de forma mais fiel e simplificar o onboarding de novos desenvolvedores.

O arquivo `docker-compose.yml` irá orquestrar os seguintes serviços:

-   **Aplicação**: O monólito modular Java/Spring Boot será executado em um contêiner. O Dockerfile da aplicação irá compilar o código e empacotá-lo em uma imagem executável.
-   **Emulador do Firestore**: Usaremos o emulador oficial do Google Cloud para o Firestore. Isso nos permite desenvolver e testar a interação com o banco de dados localmente, sem incorrer em custos e com inicialização instantânea.
-   **Emulador do Pub/Sub**: Similarmente, o emulador do Google Cloud Pub/Sub será utilizado para desenvolver e testar a lógica de mensageria e os fluxos assíncronos localmente.

Os desenvolvedores precisarão apenas do Docker e Docker Compose instalados para clonar o repositório e executar `docker-compose up` para ter um ambiente totalmente funcional.

## Pipeline de CI/CD com GitHub Actions

A pipeline de Integração Contínua e Implantação Contínua (CI/CD) será implementada usando GitHub Actions para automatizar a construção, teste e implantação da aplicação. O workflow será definido em um arquivo YAML dentro do diretório `.github/workflows/`.

A pipeline será acionada em cada `push` para a branch `main` e em `pull requests` abertos para `main`.

As principais etapas da pipeline serão:

1.  **Checkout do Código**: Baixa o código-fonte do repositório.
2.  **Configuração do Ambiente**: Configura a versão correta do Java (JDK) e o cache de dependências (Maven/Gradle) para acelerar as execuções futuras.
3.  **Execução dos Testes e Cobertura**:
    -   Inicia os emuladores do Firestore e Pub/Sub em modo de serviço para que estejam disponíveis para os testes.
    -   Executa os testes de integração da API. Estes testes irão validar os principais fluxos de negócio, fazendo requisições HTTP para a aplicação em execução e verificando as respostas e os estados no banco de dados do emulador.
    -   Gera um relatório de cobertura de código.
    -   **Validação de Cobertura**: A pipeline irá falhar se a cobertura de código for **inferior a 80%**. Isso garante que a base de código mantenha um alto padrão de qualidade e testabilidade.
4.  **Construção da Imagem Docker**: Se os testes passarem, a pipeline irá construir a imagem Docker da aplicação.
5.  **Push para o Google Artifact Registry**: A imagem Docker será tagueada e enviada para o Google Artifact Registry, nosso repositório privado de contêineres.
6.  **Implantação no Cloud Run**: A etapa final, executada apenas em merges para a branch `main`, irá implantar a nova imagem do Artifact Registry no Google Cloud Run, atualizando o serviço para a nova versão.
