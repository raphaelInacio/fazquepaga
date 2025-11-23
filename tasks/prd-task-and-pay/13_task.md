---
status: pending
---

# Task 13.0: Feature de IA: Implementar "Pacotes de Tarefas" Gerados por IA

## Overview

Esta tarefa implementa uma nova funcionalidade premium que permite aos pais gerar um conjunto coeso de tarefas com base em um objetivo de desenvolvimento. Em vez de criar tarefas uma a uma, o pai/mãe poderá fornecer um prompt como "Quero ensinar meu filho a ajudar mais na cozinha", e a IA criará um "pacote" de tarefas relacionadas, com pesos e descrições sugeridas.

**MUST READ**: Antes de começar, revise as regras de design de API em `docs/ai_guidance/rules/api-rest-http.mdc`.

## Requirements

- A funcionalidade deve ser acessível a partir do Dashboard do pai.
- A IA deve retornar uma lista estruturada de tarefas (descrição, peso sugerido, tipo).
- O pai/mãe deve poder revisar, editar e aprovar a importação do pacote de tarefas para o perfil do filho.

## Subtasks

- [ ] 13.1 No backend, criar um novo endpoint, por exemplo, `POST /api/v1/ai/task-packs`. Ele deve receber um `prompt` (o objetivo do pai) e o `childId`.
- [ ] 13.2 Implementar a lógica no `AiSuggestionService` para chamar o LLM com um prompt bem elaborado, pedindo que ele retorne um JSON com uma lista de tarefas (ex: `[{description: "Ajudar a por a mesa", weight: "LOW", type: "DAILY"}, ...]`).
- [ ] 13.3 No frontend, criar um novo botão "Gerar Pacote de Tarefas com IA" no `Dashboard.tsx` ou na página de criação de tarefas.
- [ ] 13.4 Ao clicar, abrir um modal onde o pai/mãe insere o objetivo (prompt).
- [ ] 13.5 Chamar o novo endpoint do backend e exibir as tarefas sugeridas em uma lista de revisão, permitindo que o pai/mãe edite ou remova tarefas.
- [ ] 13.6 Implementar um botão "Adicionar Tarefas" que fará um loop nas tarefas selecionadas e as criará usando o `taskService.createTask` existente.
- [ ] 13.7 Implementar testes para a nova API de backend e para o componente de UI do frontend.

## Implementation Details

A chave aqui é a engenharia do prompt enviado ao LLM. Ele deve ser instruído a retornar um JSON válido no formato esperado. A UI de revisão no frontend pode usar componentes como `Checkbox` e `Input` para permitir a edição antes da importação.

### Relevant Files

- `backend/src/main/java/com/fazquepaga/taskandpay/ai/AiController.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/ai/AiSuggestionService.java`
- `frontend/src/pages/Dashboard.tsx`
- `frontend/src/services/aiService.ts`
- `frontend/src/services/taskService.ts`

## Success Criteria

- O pai/mãe consegue abrir a interface, descrever um objetivo e receber um pacote de tarefas sugerido pela IA.
- O pai/mãe consegue editar e confirmar a criação das tarefas sugeridas.
- As tarefas são adicionadas corretamente ao perfil da criança.
- A funcionalidade é coberta por testes.
- O código é revisado e aprovado.
