# Technical Specification: Monitoramento Simplificado da Plataforma

## Executive Summary

Esta especificação técnica detalha a implementação do sistema de monitoramento técnico e analítico para a plataforma **TaskAndPay**. O principal objetivo é garantir alta observabilidade e rastreabilidade com **custo zero** durante o MVP. 

A solução resolve a falta de contexto nos logs atuais introduzindo o uso de **MDC (Mapped Diagnostic Context)** e formatação **JSON estruturada nativa do Spring Boot 3.5** em produção. Para capturar erros do frontend React sem custos e sem a complexidade de gerenciar source maps no Firebase Crashlytics Web, implementamos um endpoint de ingestão de logs do cliente que centraliza erros de tela no **Cloud Logging/Error Reporting**. O monitoramento de negócio (analytics) é implementado diretamente no Firestore utilizando o padrão de **Contadores Agregados** para evitar custos excessivos de leitura de queries complexas.

---

## System Architecture

### Domain Placement

Os componentes serão adicionados dentro da arquitetura de monólito modular do Spring Boot e na estrutura do frontend React:

*   **`com.fazquepaga.taskandpay.shared.logging`**:
    *   `LoggingContextFilter.java`: Filtro HTTP para injetar metadados de contexto no MDC.
    *   `ClientLogController.java`: API para receber erros e logs críticos do frontend.
    *   `ClientLogRequest.java`: DTO para validação de payload dos logs do cliente.
*   **`com.fazquepaga.taskandpay.shared.exception`**:
    *   Ajustes no `GlobalExceptionHandler.java` para garantir logs padronizados.
*   **`com.fazquepaga.taskandpay.shared.stats`**:
    *   `FirestoreStatsService.java` e `StatsService.java`: Serviços para gerenciamento dos contadores analíticos no Firestore.
*   **`frontend/src/components/ErrorBoundary.tsx`**:
    *   Componente de fronteira de erro para capturar quebras de UI no React e despachar para o backend.
*   **`frontend/src/lib/logger.ts`**:
    *   Wrapper de logs do cliente.

---

### Component Overview

```
┌────────────────────────────────────────────────────────────────────────┐
│                        React Frontend (Client)                         │
│                                                                        │
│   ┌───────────────────┐    ┌─────────────────┐    ┌────────────────┐   │
│   │   ErrorBoundary   │    │  Axios HTTP     │    │   Firebase     │   │
│   │ (JS Exception)    │    │ (API Latency)   │    │  Performance   │   │
└───┼─────────┬─────────┼────┼────────┬────────┼────┼────────┬───────┼───┘
              │                  │                  │        │
              │ POST /api/...    │ HTTP Requests    │        │ SDK telemetry
              ▼                  ▼                  ▼        ▼
┌─────────────┼──────────────────┼──────────────────┼────────┼───────────┐
│             │                  │                  │        │           │
│   ┌─────────▼─────────┐  ┌─────▼─────────────┐    │        │           │
│   │ClientLogController│  │LoggingContextFilter│   │        │           │
│   │                   │  │ (Injeta MDC)      │    │        │           │
│   └─────────┬─────────┘  └─────┬─────────────┘    │        │           │
│             │                  │                  │        │           │
│             └────────┬─────────┘                  │        │           │
│                      ▼ (SLF4J JSON logs)          │        │           │
│             ┌──────────────────┐                  │        │           │
│             │   stdout/stderr  │                  │        │           │
│             └────────┬─────────┘                  │        │           │
│                      │                            │        │           │
│                      │ (Auto-ingestion)           │        │           │
│                      ▼                            ▼        ▼           │
│             ┌──────────────────┐            ┌──────────────┴───────┐   │
│             │  Cloud Logging   │            │ Firebase Console     │   │
│             │ (GCP Operations) │            │ (Perf/Analytics)     │   │
│             └──────────────────┘            └──────────────────────┘   │
│                                                                        │
│                       GCP / Firebase (Cloud)                           │
└────────────────────────────────────────────────────────────────────────┘
```

*   **`LoggingContextFilter`**: Filtro HTTP que é executado no início do ciclo de vida de cada requisição. Ele adiciona um `correlationId` único para rastrear a chamada e o `userId` (se autenticado) ao contexto da thread (MDC).
*   **`ClientLogController`**: Expõe um endpoint público que recebe relatórios de erro do frontend (stacktrace, componentes envolvidos, rota ativa) e os imprime usando o logger SLF4J como `ERROR`, permitindo a auto-ingestão de exceções do cliente no **GCP Error Reporting**.
*   **`FirestoreStatsService`**: Centraliza as escritas incrementais no Firestore, encapsulando chamadas concorrentes seguras ao banco de dados para evitar requisições de contagem sob coleções inteiras.

---

## Implementation Design

### Core Interfaces

#### 1. StatsService (Gestão de Métricas Analíticas)
```java
package com.fazquepaga.taskandpay.shared.stats;

import java.util.concurrent.CompletableFuture;

public interface StatsService {
    /**
     * Incrementa atomicamente uma estatística de uso ou de negócio de uma família.
     */
    CompletableFuture<Void> incrementFamilyStat(String familyId, String field, double amount);

    /**
     * Incrementa um contador global do sistema (ex: total de prompts Vertex AI executados).
     */
    CompletableFuture<Void> incrementGlobalStat(String field, double amount);
}
```

#### 2. DTO de Logs do Cliente (ClientLogRequest)
```java
package com.fazquepaga.taskandpay.shared.logging;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record ClientLogRequest(
    @NotBlank String message,
    String stack,
    @NotBlank String component,
    @NotBlank String requestUri,
    Map<String, Object> metadata
) {}
```

---

### Data Models

#### Coleção Firestore: `/families/{familyId}/metadata/stats`
Documento do tipo chave-valor contendo os totais acumulados atualizados por gatilhos do backend.

```json
{
  "totalTasksCreated": "number (int64)",
  "totalTasksCompleted": "number (int64)",
  "totalTasksApproved": "number (int64)",
  "totalAllowancePaid": "number (double)",
  "aiSuggestionsUsed": "number (int64)",
  "lastActivityTimestamp": "timestamp"
}
```

---

### API Endpoints

#### `POST /api/v1/logs/client`
Endpoint para receber erros e falhas críticas capturados no frontend React.

*   **Request Headers**:
    *   `Content-Type: application/json`
*   **Request Body (`ClientLogRequest`)**:
    ```json
    {
      "message": "Cannot read properties of undefined (reading 'avatar')",
      "stack": "TypeError: Cannot read properties of undefined (reading 'avatar')\n at UserProfile (UserProfile.tsx:12:20)",
      "component": "UserProfile",
      "requestUri": "/dashboard/profile",
      "metadata": {
        "browser": "Chrome 125.0.0",
        "userId": "usr_90a1b2c3"
      }
    }
    ```
*   **Response**:
    *   `202 Accepted` (processamento assíncrono para evitar lentidão no frontend).
*   **Segurança**:
    *   Liberado da autenticação obrigatória (permite capturar erros no onboarding/login).
    *   Protegido por **Rate Limiter** dedicado (limite de 5 requisições por minuto por IP para evitar ataques de DoS que floodem o Cloud Logging).

---

## Integration Points

*   **Google Cloud Error Reporting**:
    *   Gatilho automático: a ferramenta lê tudo o que chega como `ERROR` com um stacktrace estruturado no stdout do Cloud Run.
    *   Integrado nativamente com a biblioteca de logging padrão do Spring.
*   **Firebase JS SDK (Performance/Analytics)**:
    *   Carregado no client React (`frontend/src/lib/firebase.ts`).
    *   Métricas de carregamento e latência de rede capturadas implicitamente.

---

## Impact Analysis

| Affected Component | Type of Impact | Description & Risk Level | Required Action |
| :--- | :--- | :--- | :--- |
| `SecurityConfig` | Config Change | Libera o endpoint `/api/v1/logs/client` de filtros restritivos de autenticação. Risco: Baixo. | Adicionar rota no `permitAll()` e configurar no `RateLimitFilter`. |
| `TaskService` & `AllowanceService` | Code Change | Introduz chamadas para `StatsService` durante transações de alteração de estado. Risco: Baixo-Médio. | Garantir atualizações assíncronas/não bloqueantes dos contadores para não atrasar a resposta das APIs principais. |
| `application-prod.properties` | Config Change | Ativa o formato JSON nativo de console em produção. Risco: Baixo. | Configurar `logging.structured.format.console=json`. |

---

## Testing Approach

### Unit Tests
*   **`LoggingContextFilterTest`**: Verificar se o filtro lê corretamente o cabeçalho `X-Correlation-Id`, se gera um UUID quando ausente, insere os campos no MDC, e garante o `MDC.clear()` no bloco `finally`.
*   **`ClientLogControllerTest`**: Testar o endpoint `/api/v1/logs/client` validando cenários de payloads incorretos (bad request), rate limiting ativo, e logs impressos corretos no stream padrão.
*   **`FirestoreStatsServiceTest`**: Testar as rotinas de incremento atômico simulando falhas de concorrência e garantindo que o `FieldValue.increment()` é invocado.

### Integration Tests
*   Verificar a integração de logs lançando exceções simuladas a partir de rotas da API e conferindo se a saída de console segue o padrão JSON estruturado (quando executado no profile de produção `prod`).

---

## Development Sequencing

### Build Order

1.  **Fase 1: Infraestrutura de Logs (Backend)**
    *   Criar o `LoggingContextFilter` e registrá-lo na cadeia de filtros do Spring.
    *   Ajustar a propriedade `logging.pattern.console` em `application.properties` para incluir o ID de correlação local.
    *   Configurar `logging.structured.format.console=json` no `application-prod.properties`.
    *   Ajustar o `LoggingAspect` para garantir o envio correto da stacktrace ao SLF4J (passando `error` no último argumento).
2.  **Fase 2: Endpoint e Coleta do Frontend**
    *   Criar o `ClientLogController` e liberar sua rota na classe `SecurityConfig` e `RateLimitFilter`.
    *   Criar o componente `ErrorBoundary.tsx` no React frontend.
    *   Integrar o Firebase SDK no frontend com o monitoramento de Performance.
3.  **Fase 3: Contadores Analíticos (Firestore Stats)**
    *   Criar o `StatsService` e implementar no `FirestoreStatsService`.
    *   Integrar as atualizações de estatísticas nos fluxos de conclusão e aprovação de tarefas, além do fluxo de criação e pagamento de assinaturas/mesadas.

### Technical Dependencies
*   Nenhuma dependência externa adicional de biblioteca (utilizando recursos nativos do Spring Boot 3.5 para JSON estruturado e o SDK Admin do Firebase/Firestore já existente no projeto).

---

## Technical Considerations

### Key Decisions
*   **Uso de Ingestão via Endpoint em vez de Firebase Crashlytics Web**:
    *   *Razão:* O SDK do Firebase Crashlytics para Web não oferece suporte oficial a upload automático de Source Maps via Vite, gerando stacktraces ilegíveis e ofuscados. Centralizar os logs de erro enviando-os para `/api/v1/logs/client` permite que o GCP Error Reporting identifique os erros, integre as linhas reais enviadas pelo client em texto limpo e envie alertas automáticos de e-mail sem custos.
*   **Contadores no Firestore**:
    *   *Razão:* Rodar queries agregadoras (ex: `COUNT`) no Firestore consome leituras pagas para cada documento escaneado. Ter contadores incrementados na escrita faz com que o dashboard do pai execute apenas 1 leitura de documento de estatísticas, economizando consideravelmente.

### Known Risks
*   **Limite de Escrita de 1/s por Documento no Firestore**:
    *   *Risco:* O Firestore limita atualizações em um único documento a 1 por segundo. Se muitos eventos ocorrerem simultaneamente para uma mesma família, a escrita do contador pode sofrer concorrência e falhar.
    *   *Mitigação:* As estatísticas são confinadas ao nível de família (`/families/{familyId}/metadata/stats`). Famílias reais raramente ultrapassam 1 escrita por minuto, eliminando qualquer risco prático do limite de 1 escrita por segundo.

### Standards Compliance
*   Em total conformidade com o `.agent/rules/logging.md`.
*   Sem logs de secrets, chaves Asaas ou senhas.
*   Uso de logs parametrizados (`log.info("...", arg)`) em todas as novas classes.
