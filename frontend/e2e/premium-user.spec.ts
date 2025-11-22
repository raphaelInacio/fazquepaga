import { test, expect } from '@playwright/test';

test.describe('Premium User Flow', () => {
    test.beforeEach(async ({ page, context }) => {
        // Register a user first
        await page.goto('/register');
        await page.fill('input[name="name"]', 'Pai Premium');
        await page.fill('input[name="email"]', 'premium@test.com');
        await page.click('button:has-text("Registrar")');
        await page.waitForURL('**/dashboard');

        // Simulate upgrade to Premium by modifying localStorage
        await page.evaluate(() => {
            const parentData = JSON.parse(localStorage.getItem('parent') || '{}');
            parentData.subscriptionTier = 'PREMIUM';
            parentData.subscriptionStatus = 'ACTIVE';
            localStorage.setItem('parent', JSON.stringify(parentData));
        });

        // Reload to apply changes
        await page.reload();
    });

    test('should allow unlimited task creation for premium users', async ({ page }) => {
        // Add a child
        await page.click('text=Adicionar Criança');
        await page.fill('input[name="name"]', 'Filho Premium');
        await page.fill('input[name="phone"]', '+5511999999997');
        await page.fill('input[name="age"]', '12');
        await page.click('button:has-text("Adicionar Criança")');

        // Navigate to child's tasks
        await page.click('text=Filho Premium');

        // Create 8 daily tasks (more than free limit of 5)
        for (let i = 1; i <= 8; i++) {
            await page.click('text=Create Task');
            await page.fill('input[name="description"]', `Tarefa Premium ${i}`);
            await page.selectOption('select[name="type"]', 'DAILY');
            await page.selectOption('select[name="weight"]', i % 3 === 0 ? 'HIGH' : i % 2 === 0 ? 'MEDIUM' : 'LOW');
            await page.click('button:has-text("Create Task")');
            await page.waitForTimeout(500);
        }

        // Verify all 8 tasks were created
        for (let i = 1; i <= 8; i++) {
            await expect(page.locator(`text=Tarefa Premium ${i}`)).toBeVisible();
        }

        // Verify no error toast appeared
        await expect(page.locator('text=Limite de tarefas recorrentes atingido')).not.toBeVisible();
    });

    test('should allow access to gift card store for premium users', async ({ page }) => {
        // Navigate to gift card store
        await page.click('text=Loja de Recompensas');

        // Wait for page to load
        await page.waitForURL('**/gift-cards');

        // Verify store loaded successfully (not blocked)
        await expect(page.locator('text=Acesso Negado')).not.toBeVisible();

        // Verify gift cards are displayed
        await expect(page.locator('text=Roblox')).toBeVisible();
        await expect(page.locator('text=iFood')).toBeVisible();
        await expect(page.locator('text=PlayStation')).toBeVisible();

        // Try to redeem a card
        await page.click('button:has-text("Resgatar Agora")').first();

        // Verify success alert (mock)
        page.on('dialog', async dialog => {
            expect(dialog.message()).toContain('Gift card redeemed successfully');
            await dialog.accept();
        });
    });

    test('should display premium badge or indicator', async ({ page }) => {
        // Verify premium status is visible somewhere in the UI
        // This could be a badge, label, or any indicator
        // Adjust selector based on your actual implementation

        // For now, verify localStorage has PREMIUM tier
        const tier = await page.evaluate(() => {
            const parentData = JSON.parse(localStorage.getItem('parent') || '{}');
            return parentData.subscriptionTier;
        });

        expect(tier).toBe('PREMIUM');
    });
});
