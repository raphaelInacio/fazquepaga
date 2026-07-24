# Review da Tarefa 7.0 - Testes End-to-End (Playwright)

## Status: CHANGES REQUESTED

## Resumo
A tarefa 7.0 consistia na implementação de testes End-to-End (E2E) para o fluxo de Gift Cards, tanto para o dependente quanto para o responsável. A implementação cobre o fluxo completo de solicitação e aprovação, além do cenário negativo de falha no pagamento.

## Critérios de Aceite
- [x] Testes E2E escritos cobrindo a vitrine do dependente e o dashboard do responsável.
- [x] Uso de mocks (Playwright `page.route`) para isolar a dependência do backend e focar na validação da UI.
- [x] Adequação do TypeScript (removido uso de `any`).
- [ ] Testes passando de forma estável na CI (atualmente apresentando falhas nos locators e no mock de APIs auxiliares).

## Detalhes do Code Review (Subagent)
**CRITICAL**
- Corrigir a falta de mock das APIs auxiliares (ex: `/api/v1/tasks`, `/api/v1/families/*/stats`) que causam quebra da página com "Oops! Algo deu errado" no React Error Boundary.

**MAJOR**
- Idioma do código: Os comentários e variáveis que estavam em PT-BR foram passados para o inglês para aderência às regras do projeto.
- Tamanho das funções: O setup dos mocks foi extraído para funções auxiliares (`setupChildMocks` e `setupParentMocks`).

**MINOR**
- Magic numbers: Substituídos timeouts repetidos por constantes `TIMEOUT_WAIT_URL` e `TIMEOUT_WAIT_LOCATOR`.

## Próximos Passos
1. Adicionar os mocks faltantes de dados relacionados às tarefas, histórico financeiro e assinaturas para estabilizar o carregamento das páginas.
2. Garantir que o Playwright localiza os elementos corretos em caso de delay de carregamento.
