# Tarefa 5.0: Frontend — Remover Menções a WhatsApp e Gift Card do Marketing e Pricing

<critical>Leia os arquivos _prd.md e techspec.md desta pasta antes de começar. Sua tarefa será invalidada se não o fizer.</critical>

## Visão Geral

Limpa o conteúdo de comunicação externa (marketing, pricing, cancelamento e i18n) de todas as referências às features removidas do MVP. Após esta tarefa, nenhum usuário verá "Integração com WhatsApp", "Loja de Gift Cards" ou "Comprovante de foto" em nenhuma tela de marketing, assinatura ou fluxo de cancelamento.

As mudanças são nas seguintes superfícies:
- **`Pricing.tsx`** e **`PricingPage.tsx`**: remover itens do array de features do plano Premium
- **`cancel-subscription-modal.tsx`**: remover o item de impacto "Loja de Gift Cards"
- **`en.json`** e **`pt.json`**: remover chaves i18n obsoletas de Gift Card, proof e WhatsApp
- **`Dashboard.tsx`**: chave `dashboard.generateCode` que referencia "Generate WhatsApp Code" (renomear para algo neutro ou remover se o botão de onboarding code for mantido)

<skills>
### Conformidade com Skills Padrão

- **[internationalization]**: remover chaves i18n sem uso; não deixar chaves órfãs nos arquivos de localização
- **[react]**: remover hardcoded strings e garantir que o build não produza avisos sobre chaves de tradução ausentes
- **[code-standards]**: zero erros ESLint após as mudanças
</skills>

<requirements>
- Remover "Integração com WhatsApp" e "Loja de Gift Cards" dos arrays de features em `Pricing.tsx` e `PricingPage.tsx`
- Remover o `<li>` de Gift Cards do modal de impacto de cancelamento em `cancel-subscription-modal.tsx`
- Remover chaves i18n obsoletas de `en.json` e `pt.json`:
  - Bloco `giftCards` em `childPortal`
  - Bloco `giftCardStore`
  - Chaves `proofRequired`, `viewProof`, `proof`, `noProof`, `requiresProof` em `childTasks`
  - Chave `generateCode` em `dashboard` (avaliar se o botão de código de onboarding ainda existe — se sim, renomear a chave para algo sem referência ao WhatsApp)
  - Chave `settings.subscription.impact.giftcards`
- `npm run build` deve concluir sem warnings de chaves i18n ausentes
</requirements>

## Subtarefas

- [ ] 5.1 Em `Pricing.tsx` (linha 59–63): remover os `<li>` de "Loja de Gift Cards" e "Integração com WhatsApp"
- [ ] 5.2 Em `PricingPage.tsx` (linhas 41–42): remover `"Loja de Gift Cards"` e `"Integração com WhatsApp"` do array `features`
- [ ] 5.3 Em `cancel-subscription-modal.tsx` (linha 129): remover o `<li>` que usa `settings.subscription.impact.giftcards`
- [ ] 5.4 Em `en.json`: remover as seguintes chaves (verificar se não há outros usos antes de remover):
  - `childPortal.giftCards` (bloco completo)
  - `giftCardStore` (bloco completo)
  - `childTasks.proofRequired`
  - `childTasks.review.proof`, `childTasks.review.noProof`
  - `childTasks.create.requiresProof`
  - `childTasks.filter.pendingApproval`
  - `dashboard.generateCode` → renomear para `dashboard.onboardingCode` e atualizar o valor para "Generate Access Code" (sem referência ao WhatsApp)
  - `settings.subscription.impact.giftcards`
- [ ] 5.5 Replicar exatamente as mesmas remoções e renomeações em `pt.json`
- [ ] 5.6 Em `Dashboard.tsx`: atualizar a chave `t("dashboard.generateCode")` para `t("dashboard.onboardingCode")` se o botão de gerar código de onboarding for mantido na UI
- [ ] 5.7 Executar `npm run build` e verificar zero erros/warnings de i18n

## Detalhes de Implementação

Consulte a **seção "Fora de Escopo (MVP)"** do [PRD](./_prd.md) e a **seção "Considerações Técnicas"** da [Tech Spec](./techspec.md).

**Sobre a chave `dashboard.generateCode`:**
O botão de geração de código de onboarding (QR/código para o filho fazer login no portal web) é uma funcionalidade **mantida** no MVP. O que muda é apenas o texto da chave, que atualmente referencia "WhatsApp Code". O botão e sua funcionalidade devem permanecer — apenas o label deve ser atualizado para algo neutro como "Gerar Código de Acesso".

**Localização no `en.json`:** linha 104  
**Localização no `pt.json`:** linha 149  
**Localização de uso no `Dashboard.tsx`:** linha 757

**Chaves a revisar com cuidado** (verificar uso antes de deletar):
- `childTasks.filter.pendingApproval` — pode estar sendo usado na lógica de filtro que ainda lê tarefas antigas com status `PENDING_APPROVAL` (ver Tarefa 4.0, subtarefa 4.3)

## Critérios de Sucesso

- Telas de Pricing (`/subscription` e componente `Pricing`) não mencionam "WhatsApp" nem "Gift Cards"
- Modal de cancelamento não lista "Gift Card store" como item de impacto
- Nenhuma chave i18n obsoleta permanece nos arquivos `en.json` e `pt.json`
- Botão de geração de código de onboarding mantém funcionalidade, com texto atualizado
- `npm run build` → zero erros

## Testes da Tarefa

- [ ] **E2E (Playwright)**: verificar que a página `/subscription` não exibe "WhatsApp" nem "Gift Cards" nos features do plano
- [ ] **E2E (Playwright)**: verificar que o modal de cancelamento não lista "Gift Card" como impacto
- [ ] **Build**: `npm run build` sem erros ou warnings de chaves ausentes

<critical>SEMPRE CRIE E EXECUTE OS TESTES DA TAREFA ANTES DE CONSIDERÁ-LA FINALIZADA</critical>

## Arquivos Relevantes

- [`Pricing.tsx`](../../frontend/src/components/Pricing.tsx) — **MODIFICAR** (remover items do plano)
- [`PricingPage.tsx`](../../frontend/src/pages/PricingPage.tsx) — **MODIFICAR** (remover features array items)
- [`cancel-subscription-modal.tsx`](../../frontend/src/components/cancel-subscription-modal.tsx) — **MODIFICAR** (remover item Gift Cards)
- [`en.json`](../../frontend/src/locales/en.json) — **MODIFICAR** (remover chaves obsoletas)
- [`pt.json`](../../frontend/src/locales/pt.json) — **MODIFICAR** (remover chaves obsoletas)
- [`Dashboard.tsx`](../../frontend/src/pages/Dashboard.tsx) — **MODIFICAR** (atualizar chave `generateCode` → `onboardingCode`)
