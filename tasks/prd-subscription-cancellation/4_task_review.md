# Review: Task 4.0 - Frontend: Cancellation Flow UI & Integration

**Revisor**: AI Code Reviewer
**Data**: 2026-05-28
**Arquivo da task**: 4_task.md
**Status**: APROVADO COM OBSERVAÇÕES

## Resumo

A implementação da interface e integração para o fluxo de cancelamento no frontend foi avaliada. A maior parte do fluxo está bem implementada, utilizando de forma adequada os componentes da shadcn-ui e lidando corretamente com os diferentes passos do modal e as chamadas à API. Contudo, existem algumas quebras de padrão de código (como nomenclatura de arquivos) e problemas significativos na suite de testes E2E recém adicionada (erros lógicos na busca por botões), o que levou à falha de grande parte da bateria de testes e, por isso, solicita-se ajustes.

## Arquivos Revisados

| Arquivo | Status | Problemas |
|---------|--------|-----------|
| `frontend/src/components/CancelSubscriptionModal.tsx` | ⚠️ Problemas | 1 |
| `frontend/src/pages/Settings.tsx` | ✅ OK | 0 |
| `frontend/src/services/subscriptionService.ts` | ✅ OK | 0 |
| `frontend/src/types/index.ts` | ✅ OK | 0 |
| `frontend/src/locales/en.json` | ✅ OK | 0 |
| `frontend/src/locales/pt.json` | ✅ OK | 0 |
| `frontend/e2e/subscription-cancellation.spec.ts` | ❌ Crítico | 1 |

## Problemas Encontrados

### 🔴 Problemas Críticos

1. **Arquivo**: `frontend/e2e/subscription-cancellation.spec.ts`
   - **Linha**: 44, 51
   - **Descrição**: O teste E2E está buscando um botão de avançar com o texto `"Continue"` (ex: `button:has-text("Continue")`), mas na interface está sendo renderizado `"Next"` em inglês ou `"Próximo"` em português (provenientes da chave `common.next`). Além disso, toda a suíte de E2E está falhando por `timeout` pois não estão conseguindo passar do login/registro (isso provavelmente necessita que o servidor backend de emuladores esteja corretamente configurado e rodando durante os testes locais).
   - **Correção Sugerida**: Mude a busca no arquivo de testes para utilizar um `data-testid` como recomendação ou procure especificamente pelo texto correto, alterando para `await page.locator('button', { hasText: 'Next' }).or(page.locator('button', { hasText: 'Próximo' })).click();`. Investigue também o porquê de os testes não completarem o `waitForURL('**/dashboard')`.

### 🟡 Problemas Major

1. **Arquivo**: `frontend/src/components/CancelSubscriptionModal.tsx`
   - **Linha**: N/A
   - **Descrição**: O nome do arquivo está em PascalCase, o que viola o padrão da arquitetura do projeto de utilizar `kebab-case` para arquivos/diretórios (conforme instrução definida em rules/code-standards.md).
   - **Correção Sugerida**: Renomear o arquivo para `cancel-subscription-modal.tsx` e atualizar os locais onde ele é importado.

### 🟢 Problemas Minor

1. **Arquivo**: `frontend/src/components/CancelSubscriptionModal.tsx`
   - **Linha**: 97-101
   - **Descrição**: A tradução para a chave `settings.subscription.reasons.OTHER_details` não é idealmente síncrona. Em PT está "Por favor, detalhe..." e em EN "Please elaborate...". É funcional, mas poderia ser refinada para ficar perfeitamente alinhada.
   - **Correção Sugerida**: Nenhum bloqueio, apenas sugestão de revisão futura.

## ✅ Destaques Positivos

- O Modal foi muito bem estruturado utilizando a biblioteca shadcn-ui.
- Ótimo uso de estados em React para lidar com os múltiplos passos da interação (Survey e Impact Warning).
- Excelente uso da biblioteca de tradução (`i18next`) em toda a interface do Modal.
- Os requests de backend estão bem alinhados com o Service Layer (subscriptionService.ts).

## Conformidade com Padrões

| Padrão | Status |
|--------|--------|
| Padrões de Código | ⚠️ (Violação do kebab-case) |
| TypeScript/Node.js | ✅ |
| REST/HTTP | ✅ |
| Logging | ✅ |
| React | ✅ |
| Testes | ❌ (E2E falhando por timeout e locator errado) |

## Recomendações

1. Renomeie `CancelSubscriptionModal.tsx` para seguir o padrão kebab-case de nomenclatura de arquivos.
2. Atualize o arquivo de testes E2E (`subscription-cancellation.spec.ts`) para buscar o label correto do botão (Next / Próximo).
3. Revise e inicialize seu emulador / ambiente backend para que o workflow E2E do Playwright passe adequadamente antes de confirmar os testes concluídos.

## Veredito

MUDANÇAS SOLICITADAS. Por favor, ajuste o nome do arquivo para o padrão do projeto e corrija o teste E2E para referenciar corretamente os botões baseados nas traduções inseridas, garantindo a execução com sucesso de todos os testes Playwright.
