import { test, expect } from '@playwright/test';

test.describe('Jornada do Responsável E2E', () => {

    test('Fluxo Principal: Registro, Login, Criar Filho, Onboarding e Criar Tarefa', async ({ page }) => {
        test.setTimeout(120000);
        page.on('console', msg => console.log('BROWSER CONSOLE:', msg.text()));
        page.on('pageerror', err => console.log('BROWSER EXCEPTION:', err.stack || err.message));

        const timestamp = Date.now();
        const email = `pai-jornada-${timestamp}@test.com`;
        const password = 'password123';
        const childId = 'child-123';

        // --- Mocking das APIs ---
        await page.route('**/api/v1/auth/register', async route => {
            await route.fulfill({
                status: 201,
                json: { id: 'parent-123', name: 'Pai Jornada E2E', email }
            });
        });

        await page.route('**/api/v1/auth/login', async route => {
            await route.fulfill({
                status: 200,
                json: {
                    token: 'mock-jwt-token',
                    refreshToken: 'mock-refresh-token',
                    user: {
                        id: 'parent-123',
                        name: 'Pai Jornada E2E',
                        email,
                        phoneNumber: '11999999999',
                        subscriptionTier: 'FREE',
                        subscriptionStatus: 'ACTIVE'
                    }
                }
            });
        });

        let childrenList: any[] = [];
        await page.route(url => url.pathname.match(/^\/api\/v1\/children\/?[^\/]*$/) !== null, async route => {
            if (route.request().method() === 'POST') {
                const child = {
                    id: childId,
                    name: 'Filho Jornada',
                    phoneNumber: '11888888888',
                    age: 10,
                    accessCode: 'CODE123'
                };
                childrenList.push(child);
                await route.fulfill({ status: 201, json: child });
            } else if (route.request().method() === 'GET') {
                const url = route.request().url();
                if (url.includes(`/children/${childId}`)) {
                    const child = childrenList.find(c => c.id === childId);
                    await route.fulfill({ status: 200, json: child || null });
                } else {
                    await route.fulfill({ status: 200, json: childrenList });
                }
            }
        });

        await page.route('**/api/v1/children/*/onboarding-code*', async route => {
            await route.fulfill({
                status: 200,
                json: { code: 'CODE123', accessCode: 'CODE123' }
            });
        });

        let tasksList: any[] = [];
        await page.route('**/api/v1/tasks*', async route => {
            if (route.request().method() === 'POST') {
                const task = {
                    id: 'task-123',
                    description: 'Tarefa Jornada 1',
                    type: 'DAILY',
                    weight: 'MEDIUM',
                    value: 5.0,
                    status: 'PENDING'
                };
                tasksList.push(task);
                await route.fulfill({ status: 201, json: task });
            } else if (route.request().method() === 'GET') {
                await route.fulfill({ status: 200, json: tasksList });
            }
        });

        await page.route('**/api/v1/subscription/status*', async route => {
            await route.fulfill({
                status: 200,
                json: {
                    tier: 'FREE',
                    status: 'ACTIVE',
                    subscriptionId: null,
                    trialActive: true,
                    trialDaysRemaining: 3
                }
            });
        });

        await page.route('**/api/v1/children/*/ledger*', async route => {
            await route.fulfill({
                status: 200,
                json: { transactions: [], balance: 0 }
            });
        });

        await page.route('**/api/v1/children/*/ledger/insights*', async route => {
            await route.fulfill({
                status: 200,
                json: { insight: 'Parabéns pelo progresso financeiro!' }
            });
        });

        await page.route('**/api/v1/allowance/predicted*', async route => {
            await route.fulfill({
                status: 200,
                json: { predicted_allowance: 0.0 }
            });
        });

        await page.route('**/api/v1/families/*/stats', async route => {
            await route.fulfill({
                status: 200,
                json: {
                    totalTasksCreated: 5,
                    totalTasksCompleted: 3,
                    totalTasksApproved: 2,
                    totalAllowancePaid: 10.0,
                    aiSuggestionsUsed: 1
                }
            });
        });

        // 1. Registro
        await page.goto('/register');
        await page.fill('input[name="name"]', 'Pai Jornada E2E');
        await page.fill('input[name="email"]', email);
        await page.fill('input[name="phoneNumber"]', '11999999999');
        await page.fill('input[name="password"]', password);
        await page.fill('input[name="confirmPassword"]', password);
        await page.click('[data-testid="register-submit-button"]');

        await page.waitForURL('**/login', { timeout: 15000 });

        // 2. Login
        await page.fill('input[type="email"]', email);
        await page.fill('input[type="password"]', password);
        await page.click('button[type="submit"]');
        await page.waitForURL('**/dashboard', { timeout: 15000 });

        // Verificar contadores de estatísticas da família no Dashboard
        await expect(page.locator('[data-testid="stats-pending-tasks"]')).toHaveText('1'); // 3 concluídas - 2 aprovadas = 1 pendente
        await expect(page.locator('[data-testid="stats-total-earned"]')).toContainText('10,00');
        await expect(page.locator('[data-testid="stats-ai-used"]')).toHaveText('1');

        // 3. Adicionar Filho
        await page.click('[data-testid="add-child-button"]');
        await page.waitForURL('**/add-child', { timeout: 10000 });
        await page.fill('input[name="name"]', 'Filho Jornada');
        await page.fill('input[name="age"]', '10');
        await page.fill('input[name="phoneNumber"]', '11888888888');
        await page.click('[data-testid="add-child-submit-button"]');
        await page.waitForURL('**/dashboard', { timeout: 15000 });

        // 4. Gerar Código de Onboarding
        await page.click('[data-testid="generate-code-button"]');
        await expect(page.locator('.font-mono')).toContainText('CODE123');
        await page.keyboard.press('Escape');

        // 5. Ir para tarefas e criar uma tarefa manual
        await page.click('text=Filho Jornada');
        await page.waitForURL(/\/child\/.*\/tasks/, { timeout: 10000 });
        await page.click('[data-testid="create-task-button"]');
        await page.fill('input[name="description"]', 'Tarefa Jornada 1');
        await page.click('[data-testid="create-task-submit-button"]');

        // Verificar tarefa na lista
        await expect(page.locator('text=Tarefa Jornada 1')).toBeVisible();
    });

    test('Fluxo Premium: Upgrade de Assinatura e Fluxo de Cancelamento', async ({ page }) => {
        const timestamp = Date.now();
        const email = `pai-premium-${timestamp}@test.com`;

        // Mock para simular usuário Premium ativo no status de assinatura
        let isPremiumUser = false;
        await page.route('**/api/v1/subscription/status*', async route => {
            await route.fulfill({
                status: 200,
                json: {
                    tier: isPremiumUser ? 'PREMIUM' : 'FREE',
                    status: 'ACTIVE',
                    subscriptionId: isPremiumUser ? 'sub_123' : null,
                    trialActive: !isPremiumUser,
                    trialDaysRemaining: isPremiumUser ? null : 3
                }
            });
        });

        await page.route('**/api/v1/families/*/stats', async route => {
            await route.fulfill({
                status: 200,
                json: {
                    totalTasksCreated: 0,
                    totalTasksCompleted: 0,
                    totalTasksApproved: 0,
                    totalAllowancePaid: 0.0,
                    aiSuggestionsUsed: 0
                }
            });
        });

        await page.route('**/api/v1/subscription/subscribe*', async route => {
            isPremiumUser = true; // Simula a ativação para as chamadas subsequentes
            await route.fulfill({
                status: 200,
                json: { checkoutUrl: 'https://sandbox.asaas.com/checkout/mock-session-id' }
            });
        });

        await page.route('**/api/v1/subscription/cancel*', async route => {
            await route.fulfill({
                status: 200,
                json: {
                    status: 'PENDING_CANCELLATION',
                    cancellationDate: new Date().toISOString(),
                    message: 'Assinatura cancelada com sucesso'
                }
            });
        });

        await page.route('**/api/v1/auth/login', async route => {
            await route.fulfill({
                status: 200,
                json: {
                    token: 'mock-jwt-token',
                    user: {
                        id: 'parent-123',
                        name: 'Pai Premium E2E',
                        email,
                        subscriptionTier: 'FREE',
                        subscriptionStatus: 'ACTIVE'
                    }
                }
            });
        });

        // 1. Login
        await page.goto('/login');
        await page.fill('input[type="email"]', email);
        await page.fill('input[type="password"]', 'password123');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/dashboard', { timeout: 15000 });

        // 2. Ir para a Pricing Page e assinar
        await page.goto('/subscription');
        await page.waitForURL('**/subscription');
        const subscribeBtn = page.getByRole('button', { name: 'Assinar Agora' });
        await expect(subscribeBtn).toBeVisible();
        await subscribeBtn.click();
        await page.waitForURL('**/sandbox.asaas.com/**', { timeout: 15000 });

        // 3. Voltar e simular o fluxo de cancelamento nas configurações
        // Force state Premium no localStorage
        await page.goto('/dashboard');
        await page.evaluate(() => {
            const userStr = localStorage.getItem('user');
            if (userStr) {
                const user = JSON.parse(userStr);
                user.subscriptionTier = 'PREMIUM';
                user.subscriptionStatus = 'ACTIVE';
                localStorage.setItem('user', JSON.stringify(user));
            }
        });
        await page.reload();

        await page.goto('/settings');
        await page.waitForURL('**/settings');

        const cancelBtn = page.locator('[data-testid="cancel-subscription-button"]');
        await expect(cancelBtn).toBeVisible();
        await cancelBtn.click();

        // Modal survey
        await expect(page.locator('text=cancelando?')).toBeVisible();
        await page.click('label[for="r1"]');
        await page.locator('button', { hasText: 'Next' }).or(page.locator('button', { hasText: 'Próximo' })).click();

        // Confirmação
        await expect(page.locator('text=Tem certeza?').or(page.locator('text=Are you sure?'))).toBeVisible();
        await page.locator('button', { hasText: 'Cancelamento' }).or(page.locator('button', { hasText: 'Cancellation' })).click();

        // Toast de sucesso
        await expect(page.locator('text=sucesso').or(page.locator('text=success'))).toBeVisible();
    });

    test('Atualização dos contadores analíticos no Dashboard após aprovação de tarefas', async ({ page }) => {
        const timestamp = Date.now();
        const email = `pai-stats-${timestamp}@test.com`;

        // Mock do login
        await page.route('**/api/v1/auth/login', async route => {
            await route.fulfill({
                status: 200,
                json: {
                    token: 'mock-jwt-token',
                    user: {
                        id: 'parent-123',
                        name: 'Pai Stats E2E',
                        email,
                        subscriptionTier: 'FREE',
                        subscriptionStatus: 'ACTIVE'
                    }
                }
            });
        });

        // Mock de filhos (1 filho)
        await page.route('**/api/v1/children*', async route => {
            await route.fulfill({
                status: 200,
                json: [{
                    id: 'child-123',
                    name: 'Filho Stats',
                    age: 10,
                    monthlyAllowance: 50.0
                }]
            });
        });

        // Mock das tarefas (1 tarefa pendente de aprovação)
        await page.route('**/api/v1/tasks*', async route => {
            await route.fulfill({
                status: 200,
                json: [{
                    id: 'task-abc',
                    description: 'Estudar Matematica E2E',
                    type: 'DAILY',
                    weight: 'MEDIUM',
                    value: 10.0,
                    status: 'PENDING_APPROVAL'
                }]
            });
        });

        // Mock do ledger e insights
        await page.route('**/api/v1/children/*/ledger*', async route => {
            await route.fulfill({
                status: 200,
                json: { transactions: [], balance: 0.0 }
            });
        });

        await page.route('**/api/v1/children/*/ledger/insights*', async route => {
            await route.fulfill({
                status: 200,
                json: { insight: 'Parabéns!' }
            });
        });

        await page.route('**/api/v1/subscription/status*', async route => {
            await route.fulfill({
                status: 200,
                json: {
                    tier: 'FREE',
                    status: 'ACTIVE',
                    trialActive: true,
                    trialDaysRemaining: 3
                }
            });
        });

        // Simulamos o estado inicial das estatísticas e o estado após aprovação de forma resiliente ao StrictMode
        let isApproved = false;
        await page.route('**/api/v1/families/*/stats', async route => {
            if (!isApproved) {
                await route.fulfill({
                    status: 200,
                    json: {
                        totalTasksCreated: 5,
                        totalTasksCompleted: 3,
                        totalTasksApproved: 2,
                        totalAllowancePaid: 20.0,
                        aiSuggestionsUsed: 2
                    }
                });
            } else {
                await route.fulfill({
                    status: 200,
                    json: {
                        totalTasksCreated: 5,
                        totalTasksCompleted: 3,
                        totalTasksApproved: 3,
                        totalAllowancePaid: 30.0,
                        aiSuggestionsUsed: 2
                    }
                });
            }
        });

        // Mock de aprovação da tarefa
        await page.route('**/api/v1/tasks/*/approve*', async route => {
            isApproved = true;
            await route.fulfill({
                status: 200,
                json: { message: 'Task approved successfully' }
            });
        });

        // Login
        await page.goto('/login');
        await page.fill('input[type="email"]', email);
        await page.fill('input[type="password"]', 'password123');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/dashboard', { timeout: 15000 });

        // Verifica os valores iniciais dos contadores no Dashboard
        await expect(page.locator('[data-testid="stats-pending-tasks"]')).toHaveText('1');
        await expect(page.locator('[data-testid="stats-total-earned"]')).toContainText('20,00');
        await expect(page.locator('[data-testid="stats-ai-used"]')).toHaveText('2');

        // Verifica que a tarefa pendente está listada na seção "Pending Approvals"
        await expect(page.locator('text=Estudar Matematica E2E')).toBeVisible();

        // Clica no botão de aprovar tarefa
        await page.click('button:has-text("Aprovar")');

        // Aguarda a tarefa sumir
        await expect(page.locator('text=Estudar Matematica E2E')).not.toBeVisible();

        // Verifica que as estatísticas da família no Dashboard foram atualizadas
        await expect(page.locator('[data-testid="stats-pending-tasks"]')).toHaveText('0');
        await expect(page.locator('[data-testid="stats-total-earned"]')).toContainText('30,00');
    });
});
