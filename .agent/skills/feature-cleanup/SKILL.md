---
name: feature-cleanup
description: Um fluxo para a remoção sistemática, completa e segura de código e features em depreciação (deprecation) no repositório TaskAndPay. Executa varreduras cruzadas de frontend, backend e infraestrutura para assegurar que não haja código órfão ou regressões na deleção. Use quando a tarefa envolver "remover", "limpar", "desligar" ou "apagar" módulos ou features.
---

# Fluxo de Feature Cleanup (Deprecation)

Siga este passo a passo rigoroso para garantir a deleção limpa e segura de uma funcionalidade:

## Passo 1: Análise de Escopo e Tracking Cruzado
1. Identifique o domínio da feature sendo removida (ex: WhatsApp, Gift Cards, etc.).
2. Faça uma busca global (`grep_search`) pelos termos da feature em:
   - `backend/src/main/java/...`
   - `frontend/src/...`
   - Arquivos de infraestrutura (`pom.xml`, `package.json`, `.env`, docker/emuladores).

## Passo 2: Limpeza do Banco de Dados / Infraestrutura
1. Verifique se há Models/Entities associados apenas a essa feature. Se sim, prepare a exclusão.
2. Identifique se havia Listeners Pub/Sub rodando. Remova os subscribers (listeners) no Spring Boot para evitar mensagens presas na fila.

## Passo 3: Limpeza do Backend
1. Delete os arquivos do pacote/módulo depreciado.
2. Remova referências a esses componentes deletados em:
   - Configurações do Spring Security.
   - Beans de Injeção de Dependência.
   - Shared Modules ou Utilitários.
3. Exclua os Testes Unitários e de Integração da feature deletada (nunca deixe os testes quebrando).

## Passo 4: Limpeza do Frontend
1. Delete os Componentes Visuais (`.tsx`, `.css`) exclusivos da feature.
2. Remova as Rotas (em arquivos de navegação ou `react-router`).
3. Limpe Hooks, Stores de Estado (Zustand, Context) e Services (Axios/Fetch) que apontavam para o backend deletado.
4. Exclua os testes unitários ou Playwright correspondentes.

## Passo 5: Verificação de Orfandade e Re-Build
1. Compile o backend (`./mvnw clean package -DskipTests`) e analise erros de importação.
2. Rode a validação de tipos no frontend (`npm run typecheck`) e o lint (`npm run lint`).
3. Assegure que nenhum componente órfão esteja importando um arquivo deletado.
4. Execute os testes restantes para garantir que funcionalidades remanescentes não foram impactadas indiretamente.
