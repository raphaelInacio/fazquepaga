# Tarefa 3.0: Frontend — Remover Gift Card Store (Rotas, Componentes, Estados)

<critical>Leia os arquivos _prd.md e techspec.md desta pasta antes de começar. Sua tarefa será invalidada se não o fizer.</critical>

## Visão Geral

Remove toda a presença da Gift Card Store do frontend. Isso inclui: a rota `/gift-cards`, a página `GiftCardStorePage`, o componente `GiftCardApprovalDialog`, o estado `pendingGiftCards` e o botão de acesso no Dashboard, e os cards/botões de navegação no `ChildPortal`. O backend (`GiftCardController`, `GiftCardService`) é **preservado intacto** para uso na Fase 2.

Após essa tarefa, nenhum usuário conseguirá navegar para `/gift-cards` via a aplicação, e nenhuma lógica de Gift Card será carregada no bundle.

<skills>
### Conformidade com Skills Padrão

- **[react]**: remoção de componentes e estados não utilizados; evitar imports órfãos
- **[code-standards]**: zero erros de TypeScript e ESLint após a mudança
- **[internationalization]**: chaves i18n de Gift Card serão removidas na Tarefa 5.0 — nesta tarefa, remover apenas os componentes que as consomem
</skills>

<requirements>
- Remover a `Route path="/gift-cards"` e o import de `GiftCardStorePage` de `App.tsx`
- Remover do `Dashboard.tsx`: estados `pendingGiftCards` e `selectedGiftCardTx`, função `handleApproveGiftCard`, chamada `giftCardService.getGiftCardRequests`, seção JSX de Gift Cards pendentes, botão "Loja de Recompensas" e import/uso de `GiftCardApprovalDialog`
- Remover os arquivos `GiftCardApprovalDialog.tsx` e `GiftCardApprovalDialog.test.tsx`
- Remover a seção de Gift Cards (card e botão de navegação para `/gift-cards`) de `ChildPortal.tsx`
- `npm run build` deve concluir sem erros de TypeScript
</requirements>

## Subtarefas

- [ ] 3.1 Em `App.tsx`: remover a linha `import { GiftCardStorePage }` e a `<Route path="/gift-cards" ...>`
- [ ] 3.2 Em `Dashboard.tsx`:
  - Remover `import { giftCardService }` e `import { GiftCardApprovalDialog }`
  - Remover estados `pendingGiftCards` e `selectedGiftCardTx`
  - Remover função `handleApproveGiftCard`
  - Remover chamada `giftCardService.getGiftCardRequests(...)` do `useEffect`
  - Remover o bloco JSX `{!isPendingLoading && pendingGiftCards.length > 0 && (...)}` (seção de Gift Cards pendentes)
  - Remover o `<Button>` que navega para `/gift-cards` (linha ~411, `data-testid="gift-cards-button"`)
  - Remover o `<GiftCardApprovalDialog ... />` no final do JSX
- [ ] 3.3 Deletar `frontend/src/components/GiftCardApprovalDialog.tsx`
- [ ] 3.4 Deletar `frontend/src/components/GiftCardApprovalDialog.test.tsx`
- [ ] 3.5 Em `ChildPortal.tsx`: remover o card/botão que chama `navigate("/gift-cards")` (linhas ~308-312 e ~529-541)
- [ ] 3.6 Executar `npm run build` — zero erros TypeScript/ESLint

## Detalhes de Implementação

Consulte a **seção "3. Desabilitar Rota `/gift-cards` no Frontend"** da [Tech Spec](./techspec.md).

**Localização dos trechos no Dashboard.tsx:**
- Import `giftCardService`: linha 23
- Import `GiftCardApprovalDialog`: linha 24
- Estados `pendingGiftCards` / `selectedGiftCardTx`: linhas 46–47
- `giftCardService.getGiftCardRequests`: linhas 140–149
- `handleApproveGiftCard`: linhas 298–307
- Botão `/gift-cards` na header: linhas 409–416
- Seção de Gift Cards pendentes no JSX: linhas 575–608
- `<GiftCardApprovalDialog />`: linha 894

**Localização no ChildPortal.tsx:**
- Botão inline na lista de tarefas: linhas 308–312
- Card "Gift Cards" na seção de resumo: linhas 529–541

**Atenção**: O ícone `Gift` importado do `lucide-react` no `Dashboard.tsx` (linha 4) pode ficar órfão após remoção do botão — remover do import se não houver outros usos.

## Critérios de Sucesso

- Navegar para `/gift-cards` retorna a tela 404 (rota não encontrada)
- Dashboard não exibe o botão "Loja de Recompensas"
- Child Portal não exibe o card/botão de Gift Cards
- `npm run build` → zero erros
- Nenhum import de `GiftCardStorePage`, `GiftCardApprovalDialog` ou `giftCardService` permanece em arquivos ativos

## Testes da Tarefa

- [ ] **Unitários/Componentes**: remover `GiftCardApprovalDialog.test.tsx` (o componente não existirá mais)
- [ ] **E2E (Playwright)**: verificar que `/gift-cards` retorna a página NotFound; verificar que o Dashboard carrega sem botão de Gift Cards
- [ ] **Build**: `npm run build` sem erros

<critical>SEMPRE CRIE E EXECUTE OS TESTES DA TAREFA ANTES DE CONSIDERÁ-LA FINALIZADA</critical>

## Arquivos Relevantes

- [`App.tsx`](../../frontend/src/App.tsx) — **MODIFICAR** (remover rota e import)
- [`Dashboard.tsx`](../../frontend/src/pages/Dashboard.tsx) — **MODIFICAR** (remover estados, handlers, JSX e imports)
- [`ChildPortal.tsx`](../../frontend/src/pages/ChildPortal.tsx) — **MODIFICAR** (remover seção Gift Cards)
- [`GiftCardApprovalDialog.tsx`](../../frontend/src/components/GiftCardApprovalDialog.tsx) — **DELETAR**
- [`GiftCardApprovalDialog.test.tsx`](../../frontend/src/components/GiftCardApprovalDialog.test.tsx) — **DELETAR**
- [`GiftCardStorePage.tsx`](../../frontend/src/pages/GiftCardStorePage.tsx) — manter arquivo (backend preservado), apenas rota removida
