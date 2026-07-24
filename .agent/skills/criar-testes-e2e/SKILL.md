---
name: criar-testes-e2e
description: Escreve testes E2E automatizados em Playwright (.spec.ts) para fluxos do TaskAndPay. Gera o boilerplate correto com isolamento de rede, configuração de mocks (ex: endpoints do Asaas, Vertex AI e Spring Boot), e adere ao uso restrito de data-testid para seletores seguros. Use quando precisar escrever testes end-to-end do zero para uma nova funcionalidade ou fluxo modificado.
---

# Procedimentos para Criação de Testes E2E (Playwright)

## Passo 1: Entendimento do Fluxo
1. Leia a Tech Spec ou os arquivos `.tsx` do fluxo alvo para entender os componentes renderizados e as interações esperadas.
2. Identifique todos os pontos de integração externa (chamadas de API para o Spring Boot ou integrações de terceiros como o Asaas).

## Passo 2: Geração do Boilerplate Seguro
1. Todo novo teste deve ser criado na pasta `frontend/e2e/` ou pasta de testes apropriada (`frontend/tests/`).
2. Utilize sempre o padrão de importação do Playwright: `import { test, expect } from '@playwright/test';`

## Passo 3: Configuração de Mocks (Isolamento de Rede)
1. Antes de iniciar qualquer navegação (`page.goto`), configure os mocks das chamadas HTTP externas utilizando `page.route` para garantir a reprodutibilidade.
2. **Exemplo de Mock do Asaas**:
   ```typescript
   await page.route('**/v3/payments', async (route) => {
     if (route.request().method() === 'POST') {
       await route.fulfill({
         status: 200,
         contentType: 'application/json',
         body: JSON.stringify({
           id: "pay_mock123",
           invoiceUrl: "https://sandbox.asaas.com/i/mock123",
           status: "PENDING"
         })
       });
     } else {
       await route.continue();
     }
   });
   ```

## Passo 4: Seleção e Interação UI
1. **Regra de Ouro**: Utilize EXCLUSIVAMENTE o método `page.getByTestId('seu-data-testid')` para interações sempre que o `data-testid` existir.
2. Se o elemento ainda não possuir um `data-testid` no código fonte do projeto, instrua o usuário a adicioná-lo ou crie o teste presumindo que ele será adicionado, evitando seletores frágeis baseados em CSS complexo ou caminhos XPath de elementos aninhados.

## Passo 5: Validação (Assertions)
1. Crie validações claras para cada etapa de tela: `await expect(page.getByTestId('sucesso-msg')).toBeVisible();`
2. Garanta que todas as premissas do caso de uso estejam cobertas em cenários de Sucesso, Erro e Casos Alternativos.
