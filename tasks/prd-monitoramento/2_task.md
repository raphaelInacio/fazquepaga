## status: completed

<task_context>
<domain>backend/shared/logging</domain>
<type>implementation</type>
<scope>core_feature</scope>
<complexity>low</complexity>
<dependencies>http_server</dependencies>
</task_context>

# Task 2.0: API de Ingestão de Logs do Frontend

## Overview

Implementar o endpoint `/api/v1/logs/client` para receber erros JavaScript do frontend React, permitindo que falhas de tela sejam centralizadas nos logs do backend e consequentemente capturadas de forma automática pelo GCP Error Reporting.

<requirements>
- Criação de `ClientLogRequest` DTO para mapear message, stack, component, requestUri e metadados.
- Criação de `ClientLogController` que loga as falhas enviadas com nível `ERROR`.
- Configuração de segurança para permitir acesso público ao endpoint (para capturar erros de usuários não autenticados).
- Configuração de rate limiter específico de 5 chamadas por minuto por IP para evitar flood de logs.
</requirements>

## Subtasks

- [x] 2.1 Criar a classe `ClientLogRequest.java` no pacote `com.fazquepaga.taskandpay.shared.logging`.
- [x] 2.2 Criar a classe `ClientLogController.java` no pacote `com.fazquepaga.taskandpay.shared.logging`.
- [x] 2.3 Adicionar a rota `/api/v1/logs/client` na liberação do `SecurityConfig.java`.
- [x] 2.4 Adicionar a rota e configurar o rate limit específico no `RateLimitFilter.java` e `RateLimitConfig.java`.
- [x] 2.5 Criar testes de integração `ClientLogControllerTest.java` validando payload inválido, rate-limit ativo e escrita correta de log via MockMvc.

## Implementation Details

Consulte a seção "API Endpoints" na [Especificação Técnica](file:///C:/Users/conta/developer/fazquepaga/tasks/prd-monitoramento/techspec.md#L112-L138).

### Relevant Files

- `backend/src/main/java/com/fazquepaga/taskandpay/shared/logging/ClientLogController.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/shared/logging/ClientLogRequest.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/config/SecurityConfig.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/security/RateLimitFilter.java`

### Dependent Files

- `tasks/prd-monitoramento/1_task.md` (Precisa da infraestrutura de MDC e JSON configurada).

## Success Criteria

- Chamada `POST /api/v1/logs/client` aceita payloads de erro e responde com status `202 Accepted`.
- O erro enviado pelo cliente é registrado nos logs do servidor.
- O rate limiter bloqueia a 6ª chamada no mesmo minuto, retornando HTTP `429 Too Many Requests`.
- Teste de integração de API executado e passando com sucesso.
