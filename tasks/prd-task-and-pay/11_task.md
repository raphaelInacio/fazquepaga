---
status: pending
---

# Task 11.0: Frontend: Criar Portal Web para Crianças (com Coach de IA e Modo Aventura)

## Overview

Esta tarefa visa criar uma primeira versão (MVP) do portal web da criança, oferecendo uma experiência rica e gamificada através do uso de IA generativa. O portal permitirá que a criança visualize suas tarefas e saldo, e interaja com um "coach financeiro" e um "modo aventura".

**MUST READ**: Antes de começar, revise as regras de React em `docs/ai_guidance/rules/react.mdc`.

## Requirements

- O portal deve ter uma rota separada (ex: `/child-portal`).
- A autenticação deve ser simples e segura para uma criança.
- A interface deve ser extremamente simples, visual e otimizada para dispositivos móveis.

## Subtasks

- [ ] 11.1 No backend, adicionar um endpoint de autenticação para a criança (ex: `POST /api/v1/children/login`).
- [ ] 11.2 Criar a página de login `frontend/src/pages/ChildLogin.tsx`.
- [ ] 11.3 Criar a página principal do portal, `frontend/src/pages/ChildPortal.tsx`.
- [ ] 11.4 Na página do portal, exibir o saldo e a lista de tarefas pendentes.
- [ ] 11.5 **(Feature de IA) Coach Financeiro:**
    - [ ] Criar uma área na UI onde a criança pode definir uma meta (ex: "Um jogo novo - R$250").
    - [ ] Criar um endpoint no backend (`POST /api/v1/ai/goal-coach`) que recebe a meta e o `childId`.
    - [ ] A IA deve retornar um plano simples em texto. Ex: "Que legal! Para seu jogo, você precisa de R$250. Com suas tarefas, você pode conseguir em 3 meses. Para ir mais rápido, que tal pedir a tarefa 'Lavar o carro' para seus pais?".
    - [ ] Exibir a resposta da IA e uma imagem gerada por IA representando a meta.
- [ ] 11.6 **(Feature de IA) Modo Aventura:**
    - [ ] Adicionar um botão "Modo Aventura" na interface.
    - [ ] Quando ativado, o frontend chama um novo endpoint (`POST /api/v1/ai/adventure-mode/tasks`) que recebe a lista de tarefas e retorna as mesmas com descrições divertidas geradas pela IA.
    - [ ] Exibir as tarefas com os nomes de aventura. Ex: "Arrume a cama" -> "Prepare sua fortaleza!".
- [ ] 11.7 Implementar o botão "Já fiz!" para marcar uma tarefa como `COMPLETED` através de um endpoint (`POST /api/v1/tasks/{taskId}/complete`).
- [ ] 11.8 Implementar testes para o login e as novas funcionalidades de IA do portal.

## Implementation Details

Este portal é uma oportunidade chave para explorar a IA. Os endpoints de IA podem ser adicionados em um novo `AiChildController`. A geração de imagem para a meta pode usar serviços como DALL-E ou o próprio Gemini.

### Relevant Files

- `frontend/src/pages/` (novos arquivos `ChildLogin.tsx`, `ChildPortal.tsx`)
- `backend/src/main/java/com/fazquepaga/taskandpay/identity/IdentityController.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/tasks/TaskController.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/ai/AiController.java` (ou novo controller)

## Success Criteria

- A criança consegue fazer login e ver suas tarefas e saldo.
- O Coach Financeiro ajuda a criança a definir uma meta e mostra um plano.
- O Modo Aventura reescreve os nomes das tarefas de forma divertida.
- O portal é responsivo e funcional em dispositivos móveis.
- O código é revisado e aprovado.
- Todos os testes passam.