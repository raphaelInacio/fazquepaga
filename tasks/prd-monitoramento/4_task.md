## status: done

<task_context>
<domain>frontend/logging</domain>
<type>implementation</type>
<scope>core_feature</scope>
<complexity>medium</complexity>
<dependencies>http_server</dependencies>
</task_context>

# Task 4.0: Coleta de Erros e Performance no Frontend (React)

## Overview

Implementar a infraestrutura de telemetria no frontend React baseada em Firebase (Performance, Analytics) e configurar um Error Boundary global para interceptar quebras de renderização e POSTar os detalhes de exceções no endpoint do backend.

<requirements>
- Inicializar o Firebase Performance SDK no client do React.
- Criar a classe de componente `ErrorBoundary.tsx` para envolver a aplicação React.
- Ao interceptar erros no `componentDidCatch`, despachar uma requisição HTTP POST para `/api/v1/logs/client` contendo a mensagem de erro, stack trace e metadados de navegação.
- Integrar opcionalmente o Firebase Analytics para capturar navegação de telas nas trocas de rota.
</requirements>

## Subtasks

- [x] 4.1 Inicializar e exportar a instância do Firebase Performance em `frontend/src/lib/firebase.ts`.
- [x] 4.2 Criar o componente `ErrorBoundary.tsx` em `frontend/src/components/ErrorBoundary.tsx`.
- [x] 4.3 Desenhar o layout visual amigável de erro ("Desculpe, ocorreu um erro") na UI do Error Boundary para não quebrar a aplicação para o usuário.
- [x] 4.4 Envolver o componente `<App />` principal no arquivo `frontend/src/main.tsx` com o `<ErrorBoundary />`.
- [x] 4.5 Criar um teste unitário React Testing Library para validar que o Error Boundary captura exceções e invoca o Axios para registrar a falha.

## Implementation Details

Consulte a seção "4. Monitoramento do Frontend (React + Vite)" na [Especificação Técnica](file:///C:/Users/conta/developer/fazquepaga/tasks/prd-monitoramento/techspec.md#L217-L248).

### Relevant Files

- `frontend/src/main.tsx`
- `frontend/src/components/ErrorBoundary.tsx`
- `frontend/src/lib/firebase.ts`

### Dependent Files

- `tasks/prd-monitoramento/2_task.md` (Depende do endpoint backend `/api/v1/logs/client` estar ativo).

## Success Criteria

- Erros disparados propositalmente em qualquer componente do React são interceptados pelo Error Boundary.
- O Error Boundary exibe uma mensagem de desculpas estilizada (em conformidade com a identidade visual do projeto).
- Ocorre uma chamada POST para `/api/v1/logs/client` contendo os dados corretos de erro.
- A suíte de testes unitários do frontend passa com sucesso.
