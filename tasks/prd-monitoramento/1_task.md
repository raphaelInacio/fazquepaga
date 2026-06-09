## status: completed

<task_context>
<domain>backend/shared/logging</domain>
<type>implementation</type>
<scope>middleware</scope>
<complexity>medium</complexity>
<dependencies>http_server</dependencies>
</task_context>

# Task 1.0: Infraestrutura de Logs com MDC e JSON no Backend

## Overview

Implementar o enriquecimento de logs via Mapped Diagnostic Context (MDC) em cada requisição HTTP e configurar a formatação estruturada JSON em ambiente produtivo, melhorando a rastreabilidade e facilitando a depuração no GCP Cloud Logging.

<requirements>
- Criação de filtro Servlet `LoggingContextFilter` inserindo correlationId, userId (se autenticado), requestUri e clientIp no MDC.
- Garantia de limpeza do MDC (`MDC.clear()`) no final da execução de cada requisição.
- Atualização em `application.properties` com formato legível para console no ambiente local.
- Habilitação de `logging.structured.format.console=json` em `application-prod.properties`.
- Ajuste no `LoggingAspect` para registrar a stacktrace completa nas exceções.
</requirements>

## Subtasks

- [x] 1.1 Criar a classe `LoggingContextFilter.java` no pacote `com.fazquepaga.taskandpay.shared.logging`.
- [x] 1.2 Registrar o filtro na cadeia do Spring para rodar após o contexto de segurança ser resolvido.
- [x] 1.3 Configurar os padrões de console em `application.properties` e JSON em `application-prod.properties`.
- [x] 1.4 Modificar o `LoggingAspect.java` para logar o objeto `Throwable` e não apenas o seu `cause` no `log.error()`.
- [x] 1.5 Criar teste unitário `LoggingContextFilterTest.java` para testar injeção de MDC e limpeza da thread.

## Implementation Details

Consulte a seção "3. Análise e Melhoria na Qualidade dos Logs" na [Especificação Técnica](file:///C:/Users/conta/developer/fazquepaga/tasks/prd-monitoramento/techspec.md#L45-L162).

### Relevant Files

- `backend/src/main/java/com/fazquepaga/taskandpay/shared/logging/LoggingContextFilter.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/shared/LoggingAspect.java`
- `backend/src/main/resources/application.properties`
- `backend/src/main/resources/application-prod.properties`

### Dependent Files

- Ninguém (Infraestrutura inicial de logs)

## Success Criteria

- Execução local imprime correlationId e userId logados na console.
- Execução com profile `prod` imprime logs no formato estruturado JSON.
- A suite de testes unitários do filtro HTTP passa com sucesso.
