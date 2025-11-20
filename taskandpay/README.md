# TaskAndPay Application

This is the backend application for the TaskAndPay platform, a modular monolith built with Spring Boot, Google Cloud Firestore, and Google Cloud Pub/Sub.

## Como Rodar Localmente

Para rodar a aplicação localmente, você precisará ter o Docker e o Docker Compose instalados em sua máquina.

1.  **Clone o repositório:**
    ```bash
    git clone <URL_DO_REPOSITORIO>
    cd taskandpay
    ```

2.  **Inicie o ambiente Docker Compose:**
    Este comando irá construir a imagem da aplicação, iniciar a aplicação Spring Boot, o emulador do Firestore e o emulador do Pub/Sub.

    ```bash
    docker-compose up --build
    ```

    A primeira vez que você executar este comando, pode levar alguns minutos para baixar as imagens e construir a aplicação.

3.  **Acesse a aplicação:**
    A aplicação estará disponível em `http://localhost:8080`.
    O emulador do Firestore estará disponível em `http://localhost:8081` (UI do emulador).
    O emulador do Pub/Sub estará disponível em `http://localhost:8085`.

## Estrutura do Projeto

O projeto é um monólito modular, com o código organizado nos seguintes pacotes principais:

-   `identity/`: Gerenciamento de usuários (pais, filhos), autenticação e segurança de perfis.
-   `tasks/`: Criação, gerenciamento e transições de estado de tarefas.
-   `allowance/`: Lógica de cálculo da mesada.
-   `ai/`: Cliente e lógica de negócios para interagir com Vertex AI (Gemini).
-   `whatsapp/`: Interações com a API do WhatsApp Business.
-   `shared/`: Utilitários e interfaces compartilhadas.

## Dependências

As principais dependências incluem:

-   Spring Boot Web
-   Lombok
-   Spring Cloud GCP Starter Data Firestore
-   Spring Cloud GCP Pub/Sub

## Testes

Para executar os testes (ainda a ser implementado):

```bash
./mvnw test
```
