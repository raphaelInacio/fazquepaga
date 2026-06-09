import { test, expect } from '@playwright/test';

test.describe('Jornada do Filho E2E', () => {

    test('Login infantil via código, visualização de tarefas e solicitação de saque', async ({ page }) => {
        const accessCode = 'CODE12';
        const childId = 'child-123';
        const parentId = 'parent-123';

        // --- Mocking das APIs ---
        await page.route('**/api/v1/children/login', async route => {
            await route.fulfill({
                status: 200,
                json: {
                    token: 'mock-child-jwt-token',
                    refreshToken: 'mock-child-refresh-token',
                    child: {
                        id: childId,
                        name: 'Filho E2E',
                        balance: 50.0, // R$ 50 de saldo
                        monthlyAllowance: 100.0,
                        age: 10,
                        parentId: parentId
                    },
                    message: 'Login realizado com sucesso'
                }
            });
        });

        // Mock para obter tarefas pendentes da criança
        await page.route('**/api/v1/tasks?child_id=*', async route => {
            await route.fulfill({
                status: 200,
                json: [
                    {
                        id: 'task-1',
                        description: 'Lavar a louça do almoço',
                        type: 'DAILY',
                        weight: 'MEDIUM',
                        value: 5.0,
                        status: 'PENDING'
                    },
                    {
                        id: 'task-2',
                        description: 'Arrumar a cama',
                        type: 'DAILY',
                        weight: 'LOW',
                        value: 2.0,
                        status: 'PENDING'
                    }
                ]
            });
        });

        // Mock para buscar dados do filho (refreshChildData)
        await page.route(`**/api/v1/children/${childId}**`, async route => {
            await route.fulfill({
                status: 200,
                json: {
                    id: childId,
                    name: 'Filho E2E',
                    balance: 35.0, // Saldo após saque
                    monthlyAllowance: 100.0,
                    age: 10,
                    parentId: parentId
                }
            });
        });

        // Mock para solicitar saque
        let requestWithdrawalBody: any = null;
        await page.route('**/api/v1/allowance/children/*/withdraw', async route => {
            requestWithdrawalBody = route.request().postDataJSON();
            await route.fulfill({
                status: 200,
                json: { message: 'Pedido de saque enviado com sucesso' }
            });
        });

        // 1. Acessar a tela de login infantil e logar
        await page.goto('/child-login');
        await page.getByRole('textbox').fill(accessCode);
        await page.click('button:has-text("Entrar! 🚀")');
        await page.waitForURL('**/child-portal', { timeout: 15000 });

        // 2. Verificar a exibição do portal e as tarefas pendentes
        await expect(page.locator('h1')).toContainText('Olá, Filho E2E! 👋');
        await expect(page.locator('text=Lavar a louça do almoço')).toBeVisible();
        await expect(page.locator('text=Arrumar a cama')).toBeVisible();

        // 3. Abrir modal de saque
        const withdrawBtn = page.getByRole('button', { name: /Sacar/i });
        await expect(withdrawBtn).toBeVisible();
        await withdrawBtn.click();

        // Verificar dialog de saque
        await expect(page.getByText('Quanto você quer sacar?')).toBeVisible();
        await expect(page.getByText('Disponível: R$ 50.00')).toBeVisible();

        // 4. Solicitar saque de R$ 15.00
        await page.locator('input[placeholder="0.00"]').fill('15');
        await page.getByRole('button', { name: 'Pedir Saque' }).click();

        // Verificar o fechamento do dialog e o toast de sucesso
        await expect(page.getByText('Pedido de saque enviado!')).toBeVisible();
        
        // Validar se o payload enviado à API foi correto
        expect(requestWithdrawalBody).toEqual({ amount: 15 });

        // Verificar se o saldo atualizou na tela para R$ 35.00 (conforme mock de refresh)
        await expect(page.locator('text=Saldo: R$ 35.00')).toBeVisible();
    });
});
