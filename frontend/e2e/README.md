# E2E Tests - Playwright

Este diretório contém os testes end-to-end (E2E) usando Playwright para validar os fluxos de usuários Free e Premium.

## Estrutura

```
e2e/
├── free-user.spec.ts      # Testes para usuários FREE
├── premium-user.spec.ts   # Testes para usuários PREMIUM
└── README.md             # Este arquivo
```

## Pré-requisitos

1. **Backend rodando**: `http://localhost:8080`
2. **Frontend rodando**: `http://localhost:8084` (ou configurado no `playwright.config.ts`)
3. **Firestore Emulator**: Rodando localmente

## Executar Testes

### Todos os testes
```bash
npm run test:e2e
```

### Com interface visual
```bash
npm run test:e2e:ui
```

### Com browser visível (headed mode)
```bash
npm run test:e2e:headed
```

### Ver relatório
```bash
npm run test:e2e:report
```

## Testes Implementados

### Free User (`free-user.spec.ts`)

1. **Registro e Adicionar Filho**
   - Registra novo usuário FREE
   - Adiciona um filho
   - Verifica dashboard

2. **Limite de 5 Tarefas**
   - Cria 5 tarefas recorrentes (DAILY)
   - Tenta criar 6ª tarefa
   - Verifica toast de erro com botão "Upgrade"

3. **Bloqueio da Loja de Gift Cards**
   - Tenta acessar `/gift-cards`
   - Verifica mensagem "Acesso Negado"

### Premium User (`premium-user.spec.ts`)

1. **Tarefas Ilimitadas**
   - Simula upgrade para PREMIUM via localStorage
   - Cria 8+ tarefas recorrentes
   - Verifica que todas foram criadas sem erro

2. **Acesso à Loja de Gift Cards**
   - Acessa `/gift-cards`
   - Verifica que 3 cards estão visíveis (Roblox, iFood, PlayStation)
   - Testa resgate de card (mock)

3. **Indicador Premium**
   - Verifica que tier está como PREMIUM no localStorage

## Configuração

A configuração do Playwright está em `playwright.config.ts`:

- **Base URL**: `http://localhost:8084`
- **Browser**: Chromium (Desktop Chrome)
- **Screenshots**: Apenas em falhas
- **Trace**: Na primeira tentativa de retry
- **Web Server**: Inicia automaticamente o dev server

## Debugging

### Modo Debug
```bash
npx playwright test --debug
```

### Ver traces
```bash
npx playwright show-trace trace.zip
```

### Screenshots e vídeos
Os screenshots de falhas ficam em `test-results/`

## CI/CD

Para rodar em CI, configure as variáveis de ambiente:

```bash
CI=true npm run test:e2e
```

Isso ativa:
- 2 retries em caso de falha
- 1 worker (execução sequencial)
- Modo headless

## Notas

- Os testes usam `localStorage` para simular upgrade Premium
- Cada teste cria novos usuários para evitar conflitos
- Os testes assumem que o backend está limpo (sem dados anteriores)
- Para testes isolados, considere usar fixtures do Playwright
