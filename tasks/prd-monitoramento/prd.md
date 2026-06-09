# Documento de Requisitos do Produto (PRD): Monitoramento Simplificado da Plataforma

## Overview

A estabilidade da plataforma TaskAndPay e a capacidade de entender as ações de pais e filhos são fundamentais para validar o MVP. Este documento define os requisitos do sistema de monitoramento técnico (observabilidade de erros e performance) e analítico (estatísticas de negócio e comportamento do usuário), projetado especificamente para operar de maneira eficiente e sem gerar custos extras (zero custo de infraestrutura), aproveitando as cotas gratuitas (Free Tier) do Google Cloud Platform (GCP) e do Firebase.

Este sistema substitui abordagens mais complexas e caras (como BigQuery e Cloud Trace) por logs ricos em contexto e consolidações diretas em documentos do Firestore.

---

## Goals

* **Estabilidade do MVP:** Detectar quebras no lado do cliente (React) e erros na API (Java Spring Boot) imediatamente, reduzindo o tempo médio de resolução (MTTR) de bugs.
* **Custo Zero de Monitoramento:** Operar 100% das ferramentas sob as cotas gratuitas mensais do GCP Free Tier e Firebase Spark Plan.
* **Agilidade no Diagnóstico:** Enriquecer cada erro logado com dados de contexto da chamada (qual usuário originou, em qual rota, IP e ID de correlação).
* **Painel Executivo Simplificado:** Disponibilizar estatísticas de produto de forma consolidada no Firestore para exibição de relatórios simples para pais e operadores.

---

## User Stories

* **Como Desenvolvedor/Operador da plataforma, eu quero...**
  * Ter logs padronizados e enriquecidos com `userId` e `correlationId` para encontrar facilmente a causa raiz de exceções.
  * Ser notificado via e-mail quando ocorrer um erro interno do servidor (5xx) não tratado.
  * Ver quais erros JavaScript estão ocorrendo no navegador de usuários reais sem que o usuário precise me enviar um reporte detalhado.
  * Acompanhar quais endpoints da API estão lentos e qual a latência média de rede observada pelos clientes.

* **Como Pai/Mãe na plataforma, eu quero...**
  * Ver relatórios consolidados rápidos (total de tarefas criadas, concluídas, aprovadas e valores pagos) no painel de controle, sem lentidão na tela.

* **Como Gestor de Produto, eu quero...**
  * Acompanhar o funil de adesão (Cadastro, Checkout Asaas) e o uso do Vertex AI por meio de relatórios analíticos de engajamento no console do Firebase.

---

## Core Features

### 1. Injeção de Contexto de Requisição (MDC - Backend)
* **O que faz:** Associa informações contextuais sobre a chamada HTTP atual às threads de logs da API Java.
* **Importância:** Elimina logs órfãos e stacktraces sem sentido, adicionando quem, quando e onde.
* **Requisitos:**
  1. Capturar `correlationId` (gerado ou extraído de header HTTP).
  2. Capturar `userId` do usuário autenticado no Spring Security.
  3. Capturar `requestUri` (a rota acessada) e `clientIp` (IP do cliente).
  4. Injetar essas chaves no MDC do SLF4J a cada requisição e limpá-las no final do ciclo HTTP.

### 2. Formatação Estruturada JSON (Backend)
* **O que faz:** Saída de log estruturada em JSON no ambiente produtivo para facilitar indexação e pesquisa no Cloud Logging.
* **Importância:** Permite filtrar no painel do GCP por qualquer campo do MDC (ex: buscar todos os logs do usuário `user-abc` ou da correlação `corr-xyz`).
* **Requisitos:**
  1. Em ambiente de desenvolvimento (`local`/`dev`), logs permanecem em modo texto legível.
  2. Em produção (`prod`), ativar o formato JSON nativo do Spring Boot 3.5.

### 3. Tratamento de Exceções Global e Notificação de Erros
* **O que faz:** Centraliza erros no backend, loga-os adequadamente e dispara alertas para o time.
* **Importância:** Evita vazamento de stacktraces para o usuário e notifica o time de forma proativa.
* **Requisitos:**
  1. `@RestControllerAdvice` captura todas as exceções não tratadas.
  2. Logar os erros 5xx com severidade `ERROR` junto com a stacktrace e o MDC ativo.
  3. Logs com severidade `ERROR` devem ser capturados pelo **Google Cloud Error Reporting** e notificados por e-mail para a equipe de desenvolvimento.

### 4. Telemetria de Frontend (Firebase Crashlytics & Performance)
* **O que faz:** Captura e agrupa falhas JS/TS no cliente e monitora a latência percebida das chamadas AJAX.
* **Importância:** Identifica bugs no cliente que não atingem a API e mede a latência real na ponta do usuário.
* **Requisitos:**
  1. Inicializar o Firebase Crashlytics no app React.
  2. Integrar um Error Boundary no React para capturar quebras de UI e reportá-las ao Crashlytics de forma amigável.
  3. Habilitar o Firebase Performance Monitoring para registrar Page Loads, Core Web Vitals e tempos de resposta do Axios para a API.
  4. Configurar upload de Source Maps do build do Vite para traduzir erros ofuscados para arquivos `.tsx` e linhas reais do código-fonte.

### 5. Contadores Consolidados (Firestore Stats)
* **O que faz:** Mantém documentos com totais acumulados de tarefas, mesadas e chamadas de IA.
* **Importância:** Fornece analytics de negócio diretamente no Firestore, evitando a leitura em massa de coleções e eliminando custos adicionais de processamento.
* **Requisitos:**
  1. Manter contadores atômicos em documentos agregados (ex: `/families/{familyId}/metadata/stats`).
  2. Atualizar contadores em blocos de transações atômicas (`FieldValue.increment()`) durantes fluxos de negócio (criação de tarefas, aprovação, pagamentos, saques e prompts de IA).

---

## User Experience

* **Fluxo do Desenvolvedor:**
  1. Erro ocorre na API ou no Client -> Notificação chega no e-mail / Firebase Console.
  2. Desenvolvedor abre o Cloud Logging -> Filtra por `correlationId` -> Visualiza todo o fluxo da requisição que falhou, incluindo IDs de entidades e usuário.
* **Fluxo do Usuário (Painel):**
  1. Pai acessa o Dashboard -> O sistema faz a leitura rápida de 1 documento de estatísticas de negócio -> O painel é renderizado instantaneamente com o total financeiro e status de tarefas.

---

## High-Level Technical Constraints

* **GCP Free Tier Limitations:**
  * Cloud Logging: Limite de 50 GiB/mês de ingestão.
  * Firestore: Limite de 50.000 leituras e 20.000 escritas diárias.
* **Zero Custo Externo:** Não utilizar ferramentas pagas (como Sentry, Datadog) ou fluxos de dados caros (BigQuery Streaming Inserts).
* **Nativo Spring Boot:** Uso dos recursos de structured logging nativos do Spring Boot 3.4/3.5, eliminando a dependência de dependências pesadas externas.

---

## Non-Goals (Out of Scope)

* Sincronização em tempo real de dados brutos de cliques para o BigQuery.
* Uso de Cloud Trace (tracing distribuído detalhado) para o MVP.
* Reembolsos de pagamentos Asaas ou transações monetárias reais via sistema.
* Dashboards complexos no Looker Studio nesta primeira etapa de validação.

---

## Phased Rollout Plan

* **MVP (Fase 1): Logs e Observabilidade Técnica (Backend)**
  * Implementação de filtro MDC, log estruturado JSON em produção, tratamento global de exceções e ativação do Google Cloud Error Reporting.
* **Fase 2: Observabilidade Client-side (Frontend)**
  * Integração de Firebase Crashlytics (com source maps), Firebase Performance Monitoring e mapeamento básico do Firebase Analytics.
* **Fase 3: Métricas de Negócio (Firestore Stats)**
  * Implementação de contadores atômicos consolidados no Firestore e exibição no Dashboard do Pai.

---

## Success Metrics

* **Custo de Infraestrutura de Monitoramento:** R$ 0,00 adicionais na fatura mensal do GCP/Firebase durante a fase de validação.
* **Tempo de Descoberta de Erros Críticos:** Notificação automática gerada em até 5 minutos após a primeira ocorrência.
* **Riqueza de Contexto:** 100% dos logs de erro contendo pelo menos `correlationId` e rota acessada.
* **Leituras analíticas de painel:** < 3 leituras de documentos do Firestore para renderizar o Dashboard de estatísticas de uma família.

---

## Risks and Mitigations

* **Risco de Escrita Concorrente no Firestore:** Duas ações atômicas incrementando estatísticas simultaneamente podem sofrer conflitos de escrita.
  * *Mitigação:* Usar `FieldValue.increment()` que delega o cálculo concorrente ao motor nativo do Firestore, minimizando chances de conflito.
* **Poluição e Excesso de Logs:** Logs gerados por bibliotecas de terceiros (como Spring Security, Hibernate) podem consumir rapidamente a cota de 50 GiB/mês.
  * *Mitigação:* Configurar filtros estritos no arquivo de logging padrão para definir o log root como `WARN` em produção, habilitando `INFO` apenas para os pacotes de negócio `com.fazquepaga.taskandpay`.
