## status: completed

<task_context>
<domain>backend/shared/exception</domain>
<type>implementation</type>
<scope>middleware</scope>
<complexity>low</complexity>
<dependencies>http_server</dependencies>
</task_context>

# Task 3.0: Tratamento Global de Erros

## Overview

Ajustar o manipulador de exceções global (`GlobalExceptionHandler`) para garantir que erros não tratados na API Java (erros 5xx) sejam logados no padrão SLF4J adequado com severidade `ERROR` contendo a stacktrace completa, de forma a acionar os alertas automáticos do GCP Error Reporting em produção.

<requirements>
- Garanta que exceções genéricas (`Exception`, `RuntimeException`, `ExecutionException`, `InterruptedException`) sejam capturadas.
- Logar erros com severidade `ERROR` passando a stacktrace completa.
- Retornar uma resposta de erro limpa (`ApiError`) para o usuário, ocultando detalhes técnicos.
</requirements>

## Subtasks

- [x] 3.1 Revisar os métodos `handleInternalServerErrors` e `handleAsaasIntegrationException` no `GlobalExceptionHandler.java`.
- [x] 3.2 Garantir que o logger capture os erros passando `ex` como último argumento para o SLF4J (ex: `log.error("Erro interno no path: {}", request.getRequestURI(), ex);`).
- [x] 3.3 Criar teste de integração para simular uma exceção lançada por um controller e verificar a resposta do `GlobalExceptionHandler`.

## Implementation Details

Consulte a seção "2. Centralização de Logs de Erro de API" na [Especificação Técnica](file:///C:/Users/conta/developer/fazquepaga/tasks/prd-monitoramento/techspec.md#L187-L210).

### Relevant Files

- `backend/src/main/java/com/fazquepaga/taskandpay/shared/exception/GlobalExceptionHandler.java`

### Dependent Files

- `tasks/prd-monitoramento/1_task.md` (Precisa das definições de logging)

## Success Criteria

- Chamadas que geram exceções não tratadas no servidor retornam HTTP `500 Internal Server Error` com JSON padronizado e limpo.
- O console/stdout do servidor registra o log estruturado em nível `ERROR` com a stacktrace completa do erro.
