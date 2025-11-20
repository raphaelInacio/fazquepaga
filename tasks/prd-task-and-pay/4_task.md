---
status: pending
---

# Tarefa 4.0: Implementação do Módulo `whatsapp`

## Visão Geral

Esta tarefa foca na integração com a API do WhatsApp Business. Ela abrange duas funcionalidades principais: o fluxo de onboarding, onde a criança se cadastra enviando um código, e o recebimento de mensagens (fotos) para marcar tarefas como concluídas.

**LEITURA OBRIGATÓRIA**: Antes de iniciar, revise as regras do projeto em `docs/ai_guidance/rules/`.

## Requisitos

-   Implementar um endpoint de webhook (`POST /api/v1/whatsapp/webhook`) para receber mensagens do provedor da API do WhatsApp.
-   Implementar a lógica de onboarding: um pai gera um código no app web, a criança envia esse código via WhatsApp para vincular seu número de telefone ao seu perfil.
-   Implementar a lógica para receber uma imagem, identificar a criança pelo número de telefone e associar a imagem a uma tarefa que requer comprovação.
-   Ao receber uma imagem para uma tarefa, publicar um evento no Pub/Sub para processamento assíncrono.

## Subtarefas

- [ ] 4.1 Implementar a interface `WhatsAppClient` para enviar mensagens através do provedor (ex: Twilio).
- [ ] 4.2 Desenvolver a lógica no `IdentityService` para gerar e armazenar um código de onboarding único e temporário para uma criança.
- [ ] 4.3 Implementar o `WhatsAppController` com o endpoint do webhook.
- [ ] 4.4 No webhook, desenvolver a lógica para:
    -   Validar a assinatura da requisição (segurança).
    -   Verificar se a mensagem é um código de onboarding e, em caso afirmativo, vincular o `phoneNumber` ao usuário `CHILD`.
    -   Verificar se a mensagem contém uma imagem.
- [ ] 4.5 Se uma imagem for recebida, identificar o usuário `CHILD` e encontrar a tarefa pendente que requer comprovação.
- [ ] 4.6 Publicar um evento `ProofSubmitted` no tópico do Pub/Sub com o `childId`, `taskId` e a URL/referência da imagem.
- [ ] 4.7 Implementar testes de integração para o webhook, usando um cliente HTTP para simular as chamadas do provedor do WhatsApp.

## Detalhes da Implementação

A gestão de estado será um desafio. Para o onboarding, um cache temporário (como Caffeine ou Redis) pode ser usado para armazenar a relação código -> childId. Para o envio de fotos, podemos assumir que a criança só tem uma tarefa com comprovação pendente por vez para simplificar o MVP.

### Arquivos Relevantes

-   `whatsapp/WhatsAppController.java`
-   `whatsapp/WhatsAppClient.java`
-   `whatsapp/WhatsAppService.java`
-   `identity/IdentityService.java`

## Critérios de Sucesso

-   O endpoint do webhook processa corretamente as chamadas simuladas.
-   O fluxo de onboarding (gerar código, enviar via WhatsApp, vincular conta) funciona de ponta a ponta nos testes.
-   Receber uma imagem de um número de telefone vinculado aciona corretamente a publicação de um evento no emulador do Pub/Sub.
-   A cobertura de testes para o módulo atinge o mínimo de 80%.
-   O código é revisado e aprovado.
-   Todos os testes passam.
