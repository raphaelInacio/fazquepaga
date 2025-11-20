---
status: pending
---

# Tarefa 2.0: Implementação dos Módulos `identity` e `tasks`

## Visão Geral

Esta tarefa foca na implementação das funcionalidades centrais de gerenciamento de usuários (pais e filhos) e de tarefas. Isso inclui a criação dos modelos de dados no Firestore, os repositórios para acesso aos dados e os endpoints da API REST para as operações CRUD básicas.

**LEITURA OBRIGATÓRIA**: Antes de iniciar, revise as regras do projeto em `docs/ai_guidance/rules/`.

## Requisitos

-   Implementar os modelos de dados para `User` e `Task` conforme a especificação técnica.
-   Criar repositórios para interagir com a coleção `users` e a subcoleção `tasks` no Firestore.
-   Expor endpoints da API para registrar pais, adicionar filhos e criar/visualizar tarefas.
-   Garantir que a relação pai-filho seja corretamente estabelecida no Firestore.

## Subtarefas

- [ ] 2.1 Implementar as entidades de dados (POJOs) para `User` e `Task`.
- [ ] 2.2 Implementar o `UserRepository` para operações CRUD na coleção `users`.
- [ ] 2.3 Implementar o `TaskRepository` para operações CRUD na subcoleção `tasks` de um usuário.
- [ ] 2.4 Criar o `IdentityController` com os endpoints `POST /api/v1/auth/register` e `POST /api/v1/children`.
- [ ] 2.5 Criar o `TaskController` com os endpoints `POST /api/v1/tasks` e `GET /api/v1/tasks`.
- [ ] 2.6 Implementar a lógica de serviço para conectar os controllers aos repositórios.
- [ ] 2.7 Implementar testes de integração da API para todos os endpoints criados, usando os emuladores.

## Detalhes da Implementação

A estrutura de dados deve seguir o "Modelo de Dados (Firestore)" da especificação técnica. As interfaces do repositório devem usar `ApiFuture` para lidar com a natureza assíncrona do Firestore.

### Arquivos Relevantes

-   `identity/User.java`
-   `identity/UserRepository.java`
-   `identity/IdentityController.java`
-   `tasks/Task.java`
-   `tasks/TaskRepository.java`
-   `tasks/TaskController.java`

## Critérios de Sucesso

-   É possível registrar um novo pai através da API.
-   É possível adicionar um filho a um pai existente.
-   É possível criar e listar tarefas para um filho específico.
-   Os dados são persistidos corretamente no emulador do Firestore com a estrutura definida.
-   A cobertura de testes para os novos endpoints atinge o mínimo de 80%.
-   O código é revisado e aprovado.
-   Todos os testes passam.
