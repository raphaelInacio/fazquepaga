# TaskAndPay

O **TaskAndPay** é uma plataforma backend projetada para auxiliar pais e filhos no gerenciamento de tarefas domésticas e mesadas. A aplicação utiliza uma arquitetura modular monolítica e integra tecnologias modernas como Inteligência Artificial (Vertex AI) e mensageria (Google Pub/Sub) para criar uma experiência fluida e automatizada.

## 🚀 Funcionalidades Principais

A aplicação é dividida em módulos de domínio focados:

*   **Identity (`identity`)**: Gerenciamento de usuários (pais e filhos), autenticação e perfis.
*   **Tasks (`tasks`)**: Ciclo de vida completo das tarefas (criação, atribuição, envio de provas, aprovação).
*   **Allowance (`allowance`)**: Motor de cálculo de mesadas baseado no cumprimento de tarefas.
*   **AI (`ai`)**:
    *   **Sugestão de Tarefas**: Utiliza IA Generativa (Gemini) para sugerir tarefas adequadas à idade da criança.
    *   **Validação de Provas**: Analisa imagens enviadas como prova de conclusão de tarefas para pré-validação automática.
*   **WhatsApp (`whatsapp`)**: Integração com WhatsApp Business para envio de provas de tarefas (fotos) e notificações.

## 🛠️ Tech Stack

*   **Linguagem**: Java 17
*   **Framework**: Spring Boot 3.5.7
*   **Banco de Dados**: Google Cloud Firestore (NoSQL)
*   **Mensageria**: Google Cloud Pub/Sub
*   **IA**: Spring AI com Google Vertex AI (Gemini)
*   **Integração**: Twilio (WhatsApp)
*   **Build**: Maven
*   **Containerização**: Docker & Docker Compose

## 📋 Pré-requisitos

*   Java 17+
*   Docker e Docker Compose
*   Maven (opcional, wrapper incluído)

## 🏃‍♂️ Como Rodar Localmente

A aplicação foi desenhada para ser executada facilmente em ambiente local utilizando emuladores do Google Cloud.

### 1. Clone o Repositório

```bash
git clone <URL_DO_REPOSITORIO>
cd taskandpay
```

### 2. Inicie a Infraestrutura (Emuladores)

Utilize o Docker Compose para subir os emuladores do Firestore e Pub/Sub:

```bash
docker-compose up -d
```

Isso iniciará:
*   **Firestore Emulator**: Porta `8081` (UI) e `8080` (gRPC).
*   **Pub/Sub Emulator**: Porta `8085`.

### 3. Execute a Aplicação

Você pode rodar a aplicação via linha de comando ou através da sua IDE favorita.

**Via Maven Wrapper:**

```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

## ⚙️ Configuração

As configurações principais estão no arquivo `src/main/resources/application.properties`.

### Emuladores (Padrão)
Por padrão, a aplicação está configurada para se conectar aos emuladores locais:

```properties
spring.cloud.gcp.firestore.emulator.enabled=true
spring.cloud.gcp.firestore.host=localhost:8080
spring.cloud.gcp.pubsub.emulator-host=localhost:8085
```

### Integrações Externas (Twilio)
Para testar a integração com WhatsApp, você precisará configurar suas credenciais do Twilio:

```properties
twilio.account-sid=SEU_ACCOUNT_SID
twilio.auth-token=SEU_AUTH_TOKEN
twilio.from-phone-number=+14155238886
```

## 🧪 Testes

O projeto inclui testes unitários e de integração. Para executá-los:

```bash
./mvnw test
```

## 📂 Estrutura do Projeto

```
src/main/java/com/fazquepaga/taskandpay
├── ai/           # Integração com Spring AI (Gemini)
├── allowance/    # Lógica de cálculo de mesada
├── identity/     # Gestão de usuários
├── shared/       # Configurações e utilitários compartilhados
├── tasks/        # Gestão de tarefas
├── whatsapp/     # Integração com Twilio/WhatsApp
└── TaskandpayApplication.java
```
