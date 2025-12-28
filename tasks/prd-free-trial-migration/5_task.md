---
status: pending
---

# Task 5.0: Testes E2E do Fluxo de Trial

## Overview

Esta task implementa testes end-to-end com Playwright para validar o fluxo completo do trial, incluindo badge, expiração e redirecionamento para checkout.

**MUST READ**: Antes de iniciar, revise as regras em:
- `docs/ai_guidance/rules/e2e-testing.md`

## Requirements

- Testes devem cobrir os cenários principais do trial
- Usar fixtures para simular diferentes estados de trial
- Validar comportamento visual e funcional

## Subtasks

- [ ] 5.1 Criar arquivo `trial.spec.ts` em `frontend/e2e/`
- [ ] 5.2 Implementar teste: badge visível durante trial ativo
- [ ] 5.3 Implementar teste: modal aparece quando trial expirado
- [ ] 5.4 Implementar teste: modal não pode ser fechado
- [ ] 5.5 Implementar teste: CTA redireciona para checkout
- [ ] 5.6 Executar todos os testes e validar que passam

## Implementation Details

### trial.spec.ts

```typescript
import { test, expect } from '@playwright/test';

test.describe('Free Trial Flow', () => {
    test('should show trial badge with days remaining', async ({ page }) => {
        // Mock API to return trial active with 2 days remaining
        await page.route('**/api/v1/subscription/status', async route => {
            await route.fulfill({
                json: {
                    tier: 'FREE',
                    status: null,
                    isTrialActive: true,
                    trialDaysRemaining: 2
                }
            });
        });
        
        await page.goto('/dashboard');
        
        // Verify badge is visible
        await expect(page.locator('text=Trial')).toBeVisible();
        await expect(page.locator('text=2 dias')).toBeVisible();
    });
    
    test('should show blocking modal when trial expired', async ({ page }) => {
        // Mock API to return expired trial
        await page.route('**/api/v1/subscription/status', async route => {
            await route.fulfill({
                json: {
                    tier: 'FREE',
                    status: null,
                    isTrialActive: false,
                    trialDaysRemaining: 0
                }
            });
        });
        
        await page.goto('/dashboard');
        
        // Verify modal is visible
        await expect(page.locator('text=Seu período de teste terminou')).toBeVisible();
        await expect(page.locator('button:has-text("Assinar")')).toBeVisible();
    });
    
    test('should not allow closing the modal', async ({ page }) => {
        await page.route('**/api/v1/subscription/status', async route => {
            await route.fulfill({
                json: {
                    tier: 'FREE',
                    status: null,
                    isTrialActive: false,
                    trialDaysRemaining: 0
                }
            });
        });
        
        await page.goto('/dashboard');
        
        // Modal should be present
        await expect(page.locator('text=Seu período de teste terminou')).toBeVisible();
        
        // Press Escape - modal should still be visible
        await page.keyboard.press('Escape');
        await expect(page.locator('text=Seu período de teste terminou')).toBeVisible();
        
        // Click outside (on backdrop) - modal should still be visible
        await page.click('.fixed.inset-0', { position: { x: 10, y: 10 } });
        await expect(page.locator('text=Seu período de teste terminou')).toBeVisible();
    });
    
    test('should redirect to checkout on CTA click', async ({ page }) => {
        await page.route('**/api/v1/subscription/status', async route => {
            await route.fulfill({
                json: { tier: 'FREE', isTrialActive: false, trialDaysRemaining: 0 }
            });
        });
        
        await page.route('**/api/v1/subscription/subscribe', async route => {
            await route.fulfill({
                json: { url: 'https://checkout.asaas.com/test-session' }
            });
        });
        
        await page.goto('/dashboard');
        
        // Click subscribe button
        const [popup] = await Promise.all([
            page.waitForEvent('popup').catch(() => null),
            page.click('button:has-text("Assinar")')
        ]);
        
        // Verify redirect or popup contains Asaas URL
        // Note: Implementation may vary based on how redirect is handled
    });
});
```

### Relevant Files

- `frontend/e2e/trial.spec.ts` (NOVO)

## Success Criteria

- [ ] Todos os testes E2E passam
- [ ] Cobertura inclui os 4 cenários principais
- [ ] Testes rodam em menos de 30 segundos
- [ ] Testes são estáveis (não flaky)
