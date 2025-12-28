# Product Requirements Document (PRD): Mecanismos de Segurança e Proteção de Custos

## Overview

O TaskAndPay é uma plataforma SaaS que gerencia tarefas e mesadas entre pais e filhos. A plataforma utiliza Firebase, APIs de IA generativa, e integrações externas (WhatsApp via Twilio, pagamentos via Asaas).

Este PRD define os mecanismos de segurança necessários para **proteger a plataforma contra uso indevido e controlar custos operacionais**, focando em rate limiting, proteção contra bots, e hardening da autenticação existente.

**Problema Principal**: Sem proteções adequadas, a plataforma está vulnerável a:
- Ataques de força bruta em endpoints de autenticação
- Abuso de APIs de IA (custo elevado por chamada)
- DDoS que escala custos de Firebase e Cloud Run
- Tokens de longa duração que amplificam riscos de vazamento

## Goals

| Objetivo | Métrica de Sucesso |
|----------|-------------------|
| Reduzir risco de custos inesperados | < 10% de variação no custo mensal projetado |
| Proteção contra DDoS | Zero downtime por ataques volumétricos |
| Proteção de endpoints de IA | 100% dos endpoints com quota por usuário |
| Hardening de autenticação | Zero secrets no código fonte |

## User Stories

### Como operador da plataforma, eu quero...
- Limitar o número de requisições por usuário/IP para evitar abuso
- Bloquear IPs que apresentem comportamento suspeito automaticamente
- Receber alertas quando o uso de API ultrapassar limites definidos
- Garantir que secrets de autenticação estejam seguros

### Como desenvolvedor, eu quero...
- Ter logs claros de tentativas bloqueadas para debugging
- Configurar limites de rate limiting via properties
- Poder ajustar quotas de IA por tipo de usuário (Free/Premium)

## Core Features

### 1. Rate Limiting por Camadas

**O que faz**: Limita o número de requisições por período em múltiplos níveis.

**Por que é importante**: Previne abuso, brute force e controla custos.

**Requisitos Funcionais**:
1. RF-01: Implementar rate limit global por IP (ex: 100 req/min)
2. RF-02: Implementar rate limit por usuário autenticado (ex: 200 req/min)
3. RF-03: Definir limites específicos para endpoints sensíveis:
   - `/api/v1/auth/**`: 10 req/min por IP (proteção brute force)
   - `/api/v1/tasks/suggest`: 5 req/min por usuário (quota IA)
   - `/api/v1/tasks/*/validate-image`: 3 req/min por usuário (quota IA)
4. RF-04: Retornar HTTP 429 (Too Many Requests) com header `Retry-After`
5. RF-05: Logar todas as requisições bloqueadas para análise

### 2. Proteção de APIs de IA

**O que faz**: Controla e limita o uso de endpoints que consomem IA generativa.

**Por que é importante**: Cada chamada de IA tem custo associado; uso descontrolado pode gerar faturas inesperadas.

**Requisitos Funcionais**:
1. RF-06: Implementar quota diária de chamadas de IA por usuário
   - Free: 10 sugestões de tarefa/dia
   - Premium: 50 sugestões de tarefa/dia
2. RF-07: Implementar circuit breaker para APIs externas de IA
3. RF-08: Retornar mensagem amigável quando quota for atingida
4. RF-09: Reset automático de quotas à meia-noite (timezone do usuário)

### 3. Hardening de Autenticação

**O que faz**: Corrige vulnerabilidades identificadas na autenticação atual.

**Por que é importante**: JWT secret hardcoded e tokens de longa duração são riscos críticos.

**Requisitos Funcionais**:
1. RF-10: Mover JWT secret para GCP Secret Manager
2. RF-11: Reduzir TTL do token de filho de 1 ano para 30 dias
3. RF-12: Implementar refresh token para renovação silenciosa
4. RF-13: Adicionar claim `iat` (issued at) em todos os tokens
5. RF-14: Implementar revogação de tokens por usuário (logout global)

### 4. Proteção contra Bots

**O que faz**: Detecta e bloqueia tráfego automatizado malicioso.

**Por que é importante**: Bots podem realizar ataques de credential stuffing e abuso de recursos.

**Requisitos Funcionais**:
1. RF-15: Integrar reCAPTCHA v3 nos endpoints:
   - `/api/v1/auth/register`
   - `/api/v1/auth/login`
   - `/api/v1/children/login`
2. RF-16: Exigir score mínimo de 0.5 para aceitar requisição
3. RF-17: Logar scores de reCAPTCHA para análise de padrões

### 5. Monitoramento e Alertas

**O que faz**: Monitora uso de recursos e alerta sobre anomalias.

**Por que é importante**: Permite resposta rápida a tentativas de abuso.

**Requisitos Funcionais**:
1. RF-18: Configurar alertas no Cloud Monitoring para:
   - Taxa de HTTP 429 > 100/hora
   - Custo diário de Firebase > limite definido
   - Latência de API > 2s (p95)
2. RF-19: Dashboard de métricas de segurança

## User Experience

### Usuários Afetados
- **Pais**: Podem ver mensagem de "limite atingido" ao usar sugestões de IA
- **Filhos**: Login pode requerer verificação adicional em casos suspeitos
- **Operadores**: Acesso a dashboard de monitoramento

### Fluxos Impactados
- Login/Registro: Adição de reCAPTCHA (invisível, score-based)
- Sugestão de Tarefas: Mensagem quando quota diária é atingida
- Validação de Imagem: Feedback de quota atingida

### Mensagens de Erro (i18n)
- `error.rate_limit`: "Muitas tentativas. Aguarde alguns minutos."
- `error.ai_quota_exceeded`: "Você atingiu o limite diário de sugestões. Tente novamente amanhã."

## High-Level Technical Constraints

| Restrição | Impacto |
|-----------|---------|
| Cloud Run (stateless) | Rate limiting requer Redis ou store compartilhado |
| Firebase (NoSQL) | Não usar Firestore para rate limiting (custo) |
| Custo como prioridade | Preferir soluções open-source (Bucket4j, Redis) |
| GCP já em uso | Usar Secret Manager, Cloud Monitoring nativos |

## Non-Goals (Out of Scope)

- ❌ MFA (Multi-factor Authentication) - Fase posterior
- ❌ WAF dedicado (Cloudflare, AWS WAF) - Custo alto para MVP
- ❌ SIEM completo - Overkill para o momento
- ❌ Criptografia de dados em repouso - Firebase já fornece

## Phased Rollout Plan

### MVP (Fase 1)
- Rate limiting básico (Bucket4j + Redis)
- Quotas de IA por usuário
- JWT Secret no Secret Manager
- Redução de TTL de tokens

**Critério de sucesso**: Zero incidentes de abuso em 30 dias

### Fase 2
- reCAPTCHA v3 em endpoints de auth
- Dashboard de monitoramento
- Alertas configurados

**Critério de sucesso**: Detecção automática de 90% das tentativas de abuso

### Fase 3
- Refresh tokens
- Revogação de tokens (logout global)
- Correção da validação Twilio

## Success Metrics

| Métrica | Baseline Atual | Meta MVP |
|---------|----------------|----------|
| Incidentes de abuso de API | Desconhecido | 0/mês |
| Variação de custo mensal | Descontrolado | < 10% |
| Tempo médio de detecção de abuso | N/A | < 5 min |
| Uptime da plataforma | 99% | 99.5% |

## Risks and Mitigations

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| Rate limiting muito agressivo impacta UX | Média | Alto | Limites configuráveis, testes A/B |
| Redis adiciona ponto de falha | Baixa | Alto | Fallback para in-memory temporário |
| reCAPTCHA bloqueia usuários legítimos | Baixa | Médio | Score threshold ajustável |
| Custo do Redis | Baixa | Baixo | Usar Memorystore tier básico |

## Open Questions

1. **Qual o budget mensal aceitável para serviços de segurança (Redis Memorystore)?**
2. **Há preferência por reCAPTCHA Enterprise vs gratuito?**
3. **Qual o TTL ideal para tokens de crianças? (7, 14, 30 dias?)**
4. **Deve-se implementar IP blocklist manual para casos graves?**

## Appendix

### Arquivos Analisados na Revisão de Segurança

| Arquivo | Status |
|---------|--------|
| `SecurityConfig.java` | ⚠️ Endpoints amplos permitidos |
| `JwtService.java` | 🔴 Secret hardcoded, TTL longo |
| `JwtAuthenticationFilter.java` | ✅ Bem estruturado |
| `AsaasWebhookController.java` | ✅ Validação de token implementada |
| `TwilioRequestValidator.java` | ⚠️ Validação incompleta |

### Tecnologias Recomendadas

| Componente | Tecnologia | Justificativa |
|------------|------------|---------------|
| Rate Limiter | Bucket4j | Open-source, Java nativo |
| Cache Distribuído | Redis (Memorystore) | Baixa latência, GCP nativo |
| Bot Detection | reCAPTCHA v3 | Free tier generoso, invisível |
| Secret Management | GCP Secret Manager | Já disponível no projeto |
| Monitoring | Cloud Monitoring + Alerts | Integração nativa |
