---
status: completed
---

# Tarefa 6.0: Configuração da Pipeline de CI/CD

## Visão Geral

Esta tarefa consiste em configurar a pipeline de Integração e Implantação Contínua (CI/CD) usando GitHub Actions. A pipeline automatizará a execução de testes, a verificação de cobertura, a construção da imagem Docker e a implantação no Google Cloud Run, garantindo um processo de entrega de software rápido e confiável.

**LEITURA OBRIGATÓRIA**: Antes de iniciar, revise as regras do projeto em `docs/ai_guidance/rules/`.

## Requisitos

-   Criar um workflow de GitHub Actions que é acionado em pushes e pull requests para a branch `main`.
-   A pipeline deve executar todos os testes de integração da API.
-   A pipeline deve falhar se a cobertura de testes for inferior a 80%.
-   Em caso de sucesso na branch `main`, a pipeline deve construir e publicar uma imagem Docker no Google Artifact Registry.
-   Após a publicação da imagem, a pipeline deve implantar a nova versão no Google Cloud Run.

## Subtarefas

- [x] 6.1 Criar o diretório `.github/workflows/` e adicionar um arquivo YAML para a pipeline (ex: `main.yml`).
- [x] 6.2 Configurar a pipeline para fazer o checkout do código e configurar o ambiente Java (JDK).
- [x] 6.3 Adicionar uma etapa que inicia os emuladores do Firestore e Pub/Sub como serviços, para que fiquem disponíveis para os testes.
- [x] 6.4 Adicionar o passo para executar os testes de integração e gerar o relatório de cobertura (usando Jacoco, por exemplo).
- [x] 6.5 Implementar o script ou ação que verifica o relatório de cobertura e faz a pipeline falhar se for menor que 80%.
- [x] 6.6 Configurar a autenticação com o Google Cloud usando secrets do GitHub.
- [x] 6.7 Adicionar as etapas para construir a imagem Docker e publicá-la no Artifact Registry (condicional à execução na branch `main`).
- [x] 6.8 Adicionar a etapa final para implantar a imagem no Google Cloud Run (condicional à execução na branch `main`).

## Detalhes da Implementação

A especificação técnica na seção "Pipeline de CI/CD com GitHub Actions" descreve as etapas necessárias. Será preciso usar ações do marketplace, como `actions/checkout`, `actions/setup-java`, e as ações oficiais do Google Cloud para autenticação, push para o GCR e implantação no Cloud Run.

### Arquivos Relevantes

-   `.github/workflows/main.yml`

## Critérios de Sucesso

-   A pipeline é acionada corretamente em um pull request e executa apenas os testes.
-   A pipeline falha se a cobertura de testes for de 79%.
-   A pipeline executa com sucesso (testes e implantação) em um merge para a branch `main`.
-   A nova versão da aplicação está em execução no Google Cloud Run após a conclusão da pipeline.
-   O workflow está bem documentado e os logs são fáceis de interpretar.
-   O código é revisado e aprovado.
