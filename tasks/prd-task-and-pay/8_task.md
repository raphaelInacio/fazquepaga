---
status: pending
---

# Task 8.0: Finalização do Backend: Expor APIs para Aprovação e Consulta de Dados

## Overview

Esta tarefa foca em corrigir os GAPs da API do backend, que foram identificados como críticos para a estabilidade e funcionalidade do frontend. Iremos implementar endpoints REST para aprovação de tarefas e para consulta de entidades, removendo a necessidade de o frontend depender de soluções frágeis como `localStorage`.

**MUST READ**: Antes de começar, revise as regras de design de API em `docs/ai_guidance/rules/api-rest-http.mdc`.

## Requirements

- A API deve seguir os padrões RESTful.
- Todos os novos endpoints devem ter cobertura de testes de unidade e integração.
- Os endpoints de consulta devem retornar DTOs apropriados, evitando expor entidades internas do domínio.
- **Autorização Robusta**: Todos os endpoints que acessam ou modificam dados de usuários/filhos devem implementar validação de autorização para garantir que apenas o pai correto possa acessar seus filhos.

## Subtasks

- [ ] 8.1 No `TaskController`, implementar o endpoint `POST /api/v1/tasks/{taskId}/approve`. Este endpoint deve receber o ID da tarefa, validar se o usuário (pai) tem permissão para aprová-la, e alterar o status da tarefa para `APPROVED`. **A lógica de aprovação deve ser atômica: a atualização do status da tarefa e a criação da entrada no `ledger` (extrato financeiro) devem ocorrer em uma única transação, garantindo consistência.**
- [ ] 8.2 Criar um novo `UserController` (ou renomear `IdentityController`) para gerenciar endpoints relacionados a usuários (pais e filhos). Implementar o endpoint `GET /api/v1/children/{childId}`. Este endpoint deve retornar os detalhes de uma criança específica, para que o frontend possa carregar os dados de forma confiável.
- [ ] 8.3 Criar um novo `LedgerController`. Implementar o endpoint `GET /api/v1/children/{childId}/ledger`. Este endpoint deve retornar uma lista de transações financeiras (créditos por tarefas aprovadas) para a criança.
- [ ] 8.4 Implementar testes de unidade e integração para todos os novos endpoints, garantindo que a lógica de negócio, a autorização e a resposta da API estejam corretas.

## Implementation Details

Conforme a especificação técnica (`_techspec.md`), a ausência desses endpoints é um risco técnico.

- **Para 8.1**: A lógica de aprovação deve mover o valor correspondente da tarefa para o "saldo" da criança, criando uma entrada no `ledger`.
- **Para 8.2**: O endpoint deve garantir que apenas o pai da criança possa acessá-lo, utilizando o ID do usuário autenticado para a verificação de propriedade.

### Relevant Files

- `backend/src/main/java/com/fazquepaga/taskandpay/tasks/TaskController.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/identity/IdentityController.java` (a ser renomeado/refatorado)
- `backend/src/main/java/com/fazquepaga/taskandpay/allowance/AllowanceController.java` (a lógica do ledger pode ser extraída para o novo LedgerController)
- `backend/src/main/java/com/fazquepaga/taskandpay/allowance/AllowanceService.java`
- Novos arquivos para `UserController` e `LedgerController`.

## Success Criteria

- O endpoint de aprovação de tarefa funciona e atualiza o estado da tarefa e o extrato corretamente de forma atômica.
- O endpoint de consulta de criança retorna os dados esperados, com autorização adequada.
- O endpoint de consulta de extrato retorna o histórico de transações, com autorização adequada.
- A cobertura de testes para os arquivos modificados é mantida ou aumentada.
- O código é revisado e aprovado.
- Todos os testes da pipeline de CI/CD passam.