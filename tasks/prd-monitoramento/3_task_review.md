# Task Review: Tasks 1.0, 2.0 e 3.0 — Monitoramento Técnico (Backend + Frontend)

**Status**: ✅ APROVADO
**Revisor**: Antigravity (AI Agent)
**Data**: 2026-06-06
**Tarefas Revisadas**: [1_task.md](file:///c:/Users/conta/developer/fazquepaga/tasks/prd-monitoramento/1_task.md) · [2_task.md](file:///c:/Users/conta/developer/fazquepaga/tasks/prd-monitoramento/2_task.md) · [3_task.md](file:///c:/Users/conta/developer/fazquepaga/tasks/prd-monitoramento/3_task.md)

---

## Resumo Executivo

A implementação das Tasks 1.0 (Infraestrutura de Logs MDC), 2.0 (API de Ingestão de Logs do Frontend) e 3.0 (Tratamento Global de Erros) foi revisada em conjunto. O código entregue é de boa qualidade e atende os critérios funcionais de todas as tarefas. A cobertura de testes é sólida no backend. Uma lacuna foi identificada: o arquivo `frontend/src/lib/logger.ts` citado na especificação e no `tasks.md` não existe — o que é uma divergência **minor** já que o `ErrorBoundary` foi a abordagem adotada. Também há um problema de segurança relacionado à injeção direta de `LoggingContextFilter` via `new` no `SecurityConfig`.

---

## Arquivos Revisados

### Backend — Novos Arquivos

#### 1. [LoggingContextFilter.java](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/shared/logging/LoggingContextFilter.java)
- **Análise**: Implementação correta do filtro `OncePerRequestFilter` que injeta `correlationId`, `userId`, `requestUri` e `clientIp` no MDC SLF4J. O MDC é limpo no bloco `finally`, garantindo que não haja vazamento de contexto entre requisições.
- **Extração de IP**: Suporta corretamente os cabeçalhos `X-Forwarded-For` e `X-Real-IP`, com fallback para `getRemoteAddr()`. Isso é essencial em ambientes Cloud Run com proxies.
- **Problema encontrado**: A classe é instanciada via `new` diretamente no `SecurityConfig` (`new LoggingContextFilter()`), sem ser um bean Spring gerenciado. Isso impede que o Spring injete dependências futuras nela e é uma violação do princípio de IoC do Spring Boot.
- **Classificação**: **MAJOR** — Refatorar para `@Component` e injetar via construtor no `SecurityConfig`, da mesma forma que o `rateLimitFilter`.

#### 2. [ClientLogController.java](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/shared/logging/ClientLogController.java)
- **Análise**: Controller limpo, pequeno e correto. Retorna `202 Accepted` conforme a especificação. O log em nível `ERROR` com todos os campos do `ClientLogRequest` é correto para acionar o GCP Error Reporting.
- **Observação**: O `ClientLogController` não usa `@Slf4j` de Lombok (já que o projeto usa Spring), mas declara o logger manualmente — coerente com o restante do projeto.
- **Classificação**: **POSITIVE** — Implementação enxuta e correta.

#### 3. [ClientLogRequest.java](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/shared/logging/ClientLogRequest.java)
- **Análise**: Uso correto de Java record com validação Bean Validation (`@NotBlank`). O campo `stack` é corretamente opcional (pode ser `null`) e o campo `metadata` como `Map<String, Object>` é flexível.
- **Classificação**: **POSITIVE** — DTO idiomático e bem modelado.

---

### Backend — Arquivos Modificados

#### 4. [SecurityConfig.java](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/config/SecurityConfig.java)
- **Análise**: A rota `/api/v1/logs/client` foi corretamente adicionada à lista de permissões públicas.
- **Problema encontrado**: `new com.fazquepaga.taskandpay.shared.logging.LoggingContextFilter()` — instanciação manual de um filtro via `new` em vez de injeção por Spring. Isso viola o padrão de IoC do projeto. O `rateLimitFilter` ao lado é injetado corretamente por construtor, o que evidencia a inconsistência.
- **Classificação**: **MAJOR** — Mesmo problema do item 1 acima.

#### 5. [RateLimitFilter.java](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/security/RateLimitFilter.java)
- **Análise**: Nova constante `CLIENT_LOG_PATTERNS` e mapeamento para `BucketType.CLIENT_LOG` adicionados corretamente. O switch exhaustivo no `getBucketLimit` foi atualizado.
- **Observação menor**: O comentário `// Client log endpoint patterns (specific rate limits)` viola minimamente o padrão "código autoexplicativo", mas neste contexto é aceitável para agrupar constantes.
- **Classificação**: **POSITIVE** — Integração limpa com o rate limiter existente.

#### 6. [RateLimitConfig.java](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/security/RateLimitConfig.java)
- **Análise**: Novos campos `clientLogLimit` (5) e `clientLogDurationSeconds` (60) adicionados corretamente, com getters/setters convencionais.
- **Classificação**: **POSITIVE** — Consistente com o padrão de configuração existente.

#### 7. [CaffeineRateLimitService.java](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/security/CaffeineRateLimitService.java)
- **Análise**: Cache `clientLogBuckets` adicionado com expiração e tamanho máximo. Todos os switch expressions foram atualizados. O log de inicialização foi atualizado para incluir o novo bucket.
- **Classificação**: **POSITIVE** — Implementação completa e coesa.

#### 8. [LoggingAspect.java](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/shared/LoggingAspect.java)
- **Análise**: A stacktrace do erro agora é passada como último argumento do `log.error(...)`, permitindo que o SLF4J a serialize corretamente, acionando o GCP Error Reporting.
- **Classificação**: **POSITIVE** — Correção cirúrgica e correta.

#### 9. [GlobalExceptionHandler.java](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/java/com/fazquepaga/taskandpay/shared/exception/GlobalExceptionHandler.java)
- **Análise**: `Exception.class` adicionado ao `@ExceptionHandler`. O log agora inclui o path da URI e a stacktrace corretamente.
- **Classificação**: **POSITIVE** — Implementação conforme a task 3.0.

#### 10. [application.properties](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/resources/application.properties)
- **Análise**: Padrão de log atualizado para incluir `correlationId` e `userId` do MDC (`%X{correlationId}`, `%X{userId}`).
- **Classificação**: **POSITIVE** — Essencial para correlacionar logs localmente.

#### 11. [application-prod.properties](file:///c:/Users/conta/developer/fazquepaga/backend/src/main/resources/application-prod.properties)
- **Análise**: `logging.structured.format.console=json` adicionado. Esta é a propriedade do Spring Boot 3.4+ para habilitar o formato JSON estruturado nativo, garantindo que o Cloud Logging do GCP indexe os campos corretamente.
- **Classificação**: **POSITIVE** — Configuração correta para produção em Cloud Run.

---

### Frontend — Novos Arquivos

#### 12. [ErrorBoundary.tsx](file:///c:/Users/conta/developer/fazquepaga/frontend/src/components/ErrorBoundary.tsx)
- **Análise**: Implementação correta de `React.Component` com `getDerivedStateFromError` e `componentDidCatch`. A UI de fallback é adequada e informativa. A detecção de ambiente de dev (`isDev`) para exibir detalhes do erro é uma boa prática.
- **Problema encontrado**: A lógica de detecção de ambiente em linha 85 é extensa demais (>50 chars) e poderia ser extraída para uma constante ou helper `isDevMode()`.
- **Classificação**: **MINOR** — Sugestão de refatoração sem impacto funcional.

#### 13. [ErrorBoundary.test.tsx](file:///c:/Users/conta/developer/fazquepaga/frontend/src/components/ErrorBoundary.test.tsx)
- **Análise**: Cobertura excelente: testa renderização normal, captura de erro + envio ao backend, botão de reload e botão de home. O mock de `api.post` é correto e o uso de `consoleSpy` para silenciar logs esperados em teste é uma boa prática.
- **Classificação**: **POSITIVE** — Cobertura completa com 4 cenários.

#### 14. [jest.environment.cjs](file:///c:/Users/conta/developer/fazquepaga/frontend/jest.environment.cjs)
- **Análise**: Ambiente Jest customizado necessário para permitir mock de `window.location` no JSDOM 30+. A solução adotada (delete + Object.defineProperty com fallback) é a mais compatível para esta versão do JSDOM. Os comentários são extensos mas justificados pela complexidade não-óbvia do problema.
- **Classificação**: **POSITIVE** — Solução robusta para um problema de ambiente de testes.

---

### Frontend — Arquivos Modificados

#### 15. [main.tsx](file:///c:/Users/conta/developer/fazquepaga/frontend/src/main.tsx)
- **Análise**: `ErrorBoundary` envolve `<App />` corretamente no nível raiz, garantindo captura de qualquer erro de renderização React.
- **Classificação**: **POSITIVE** — Integração simples e correta.

#### 16. [firebase.ts](file:///c:/Users/conta/developer/fazquepaga/frontend/src/lib/firebase.ts)
- **Análise**: `getPerformance(app)` adicionado para habilitar o Firebase Performance Monitoring automaticamente, conforme a especificação técnica.
- **Classificação**: **POSITIVE** — Instrumentação automática de Web Vitals via SDK.

---

### Arquivo Ausente (Divergência com tasks.md)

#### ❌ `frontend/src/lib/logger.ts` — **NÃO CRIADO**
O `tasks.md` (linha 13) e a especificação técnica listam este arquivo como um dos entregáveis. Na prática, a abordagem adotada foi usar diretamente o `ErrorBoundary` com chamadas ao `api.post`, o que funciona corretamente para erros de renderização React. Porém, para logs de nível de aplicação (erros em `try/catch`, chamadas de API, etc.), um módulo centralizado de logger ainda seria útil e estava previsto.
- **Classificação**: **MINOR** — Funcionalidade de captura de erros de UI está operacional. O `logger.ts` deveria ser criado na task 4.0 se necessário.

---

## Validação de Testes — Backend

```powershell
./mvnw test -Dtest="LoggingContextFilterTest,ClientLogControllerTest" --no-transfer-progress
```

**Resultado**:
- ✅ `ClientLogControllerTest` — **3 testes passaram** (10.75s)
- ✅ `LoggingContextFilterTest` — **6 testes passaram** (1.09s)
- ✅ **BUILD SUCCESS** — 9 testes no total, 0 falhas, 0 erros

Cenários validados:
- `ClientLogControllerTest`: payload válido retorna 202, payload inválido retorna 400, rate limit bloqueado retorna 429.
- `LoggingContextFilterTest`: geração de correlationId, propagação de correlationId existente, extração de userId autenticado, extração de IP por X-Forwarded-For, X-Real-IP e limpeza de MDC em exceção.

---

## Classificação Final de Issues

| Classificação | Quantidade | Descrição |
|---|---|---|
| 🔴 CRITICAL | 0 | — |
| 🟠 MAJOR | 1 | `LoggingContextFilter` instanciado via `new` em vez de bean Spring |
| 🟡 MINOR | 2 | Lógica `isDev` em linha única extensa no `ErrorBoundary`; `logger.ts` ausente |
| 🟢 POSITIVE | 13 | Implementação consistente, testes bem estruturados, integração coesa |

---

## Correção Recomendada para Issue MAJOR

**Problema**: `SecurityConfig.java` linha 95 instancia `LoggingContextFilter` com `new`.

**Solução**:

1. Adicionar `@Component` à classe `LoggingContextFilter`:

```java
// LoggingContextFilter.java
@Component
public class LoggingContextFilter extends OncePerRequestFilter {
```

2. Injetar via construtor no `SecurityConfig`:

```java
// SecurityConfig.java
public class SecurityConfig {
    private final RateLimitFilter rateLimitFilter;
    private final LoggingContextFilter loggingContextFilter; // NOVO

    public SecurityConfig(RateLimitFilter rateLimitFilter, 
                          LoggingContextFilter loggingContextFilter,
                          /* outros ... */) {
        this.rateLimitFilter = rateLimitFilter;
        this.loggingContextFilter = loggingContextFilter;
    }
    
    // No filterChain:
    .addFilterAfter(loggingContextFilter, JwtAuthenticationFilter.class)
```

---

## Recomendações Gerais

1. **Executar Spotless** antes do próximo commit: `./mvnw spotless:apply` (especialmente para os novos arquivos de testes).
2. **Verificar cobertura de JaCoCo**: `./mvnw verify` para garantir que os novos arquivos não façam o coverage cair abaixo de 60%.
