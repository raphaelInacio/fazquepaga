---
status: pending
---

# Tarefa 5.0: Implementação do Módulo `ai` e Validador Assíncrono

## Visão Geral

Esta tarefa abrange a integração com a IA generativa (Google Gemini) para duas funcionalidades: sugestão de tarefas para os pais e validação de imagens enviadas pelas crianças. A validação de imagem será implementada em um fluxo assíncrono usando Pub/Sub para não bloquear a thread principal.

**LEITURA OBRIGATÓRIA**: Antes de iniciar, revise as regras do projeto em `docs/ai_guidance/rules/`.

## Requisitos

-   Implementar um cliente para a API do Vertex AI para interagir com o modelo Gemini.
-   Criar uma função (ou serviço) que escuta mensagens no tópico `ProofSubmitted` do Pub/Sub.
-   No recebimento de uma mensagem, o serviço deve buscar a imagem e a descrição da tarefa.
-   O serviço deve então chamar o modelo de visão do Gemini para pré-validar se a imagem corresponde à tarefa.
-   O status da tarefa no Firestore deve ser atualizado com o resultado da validação da IA.
-   Implementar um endpoint para os pais obterem sugestões de tarefas geradas por IA.

## Subtarefas

- [ ] 5.1 Implementar a interface `AiValidator` e sua classe de implementação, que conterá a lógica de prompt para o Gemini Vision.
- [ ] 5.2 Criar um serviço (`AiSuggestionService`) que gera prompts para o Gemini Text para sugerir tarefas com base na idade da criança (a ser adicionada ao modelo `User`).
- [ ] 5.3 Criar um `AiController` com um endpoint `GET /api/v1/tasks/suggestions` que use o `AiSuggestionService`.
- [ ] 5.4 Criar um worker/listener de Pub/Sub (pode ser uma Cloud Function ou um @Service do Spring Boot com a devida configuração) que assina o tópico de comprovação de tarefas.
- [ ] 5.5 Implementar a lógica no worker para:
    -   Analisar a mensagem do Pub/Sub.
    -   Chamar o `AiValidator` para obter a pré-validação.
    -   Atualizar o documento da tarefa no Firestore para o status `PENDING_APPROVAL` com um campo `aiValidated: true/false`.
- [ ] 5.6 Implementar testes de integração para o fluxo assíncrono, publicando uma mensagem no emulador do Pub/Sub e verificando se o documento no emulador do Firestore é atualizado corretamente.
- [ ] 5.7 Implementar testes unitários para o `AiValidator` e `AiSuggestionService`, mockando o cliente do Vertex AI.

## Detalhes da Implementação

A engenharia de prompt será crucial. Para a validação de imagem, o prompt deve ser claro, instruindo a IA a agir como um "inspetor" e retornar uma resposta booleana ou um JSON simples. Ex: "Esta imagem contém uma 'cama arrumada'? Responda apenas com 'sim' ou 'não'".

### Arquivos Relevantes

-   `ai/AiValidator.java`
-   `ai/AiSuggestionService.java`
-   `ai/AiController.java`
-   `ai/TaskProofListener.java` (ou nome similar para o worker de Pub/Sub)

## Critérios de Sucesso

-   O endpoint de sugestão de tarefas retorna uma lista de tarefas relevantes geradas pela IA.
-   Publicar uma mensagem no tópico do Pub/Sub aciona o worker, que por sua vez atualiza o Firestore.
-   Os prompts para a IA são bem definidos e versionados no código.
-   A cobertura de testes para o novo fluxo atinge o mínimo de 80%.
-   O código é revisado e aprovado.
-   Todos os testes passam.
