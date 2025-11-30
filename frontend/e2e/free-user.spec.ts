import { test, expect } from '@playwright/test';

test.describe('Free User Flow', () => {
    test.setTimeout(60000); // Increase timeout for slow backend
    test('should register a free user and add a child', async ({ page }) => {
        // Navigate to homepage
        await page.goto('/');

        // Click "Começar" button
        await page.click('text=Começar gratuitamente');

        // Fill registration form
        await page.fill('input[name="name"]', 'Pai Free E2E');
        await page.fill('input[name="email"]', 'free-e2e@test.com');

        // Submit form
        await page.click('button:has-text("Registrar")');

        // Wait for redirect to dashboard
        await page.waitForURL('**/dashboard');

        // Verify dashboard loaded
        await expect(page.locator('h1')).toContainText('Dashboard');

        // Add a child
        await page.click('text=Adicionar Criança');
        await page.fill('input[name="name"]', 'Filho E2E');
        await page.fill('input[name="phone"]', '+5511999999999');
        await page.fill('input[name="age"]', '10');
        await page.click('button:has-text("Adicionar Criança")');

        // Verify child was added
        await expect(page.locator('text=Filho E2E')).toBeVisible();
    });

    test('should enforce 5 task limit for free users', async ({ page, context }) => {
        // Setup: Register and add child
        await page.goto('/register');
        await page.fill('input[name="name"]', 'Pai Free Limit');
        await page.fill('input[name="email"]', 'free-limit@test.com');
        await page.click('button:has-text("Registrar")');
        await page.waitForURL('**/dashboard');

        // Add child
        await page.click('text=Adicionar Criança');
        await page.fill('input[name="name"]', 'Filho Limit');
        await page.fill('input[name="phone"]', '+5511999999998');
        await page.fill('input[name="age"]', '8');
        await page.click('button:has-text("Adicionar Criança")');

        // Navigate to child's tasks
        await page.click('text=Filho Limit');

        // Create 5 daily tasks
        for (let i = 1; i <= 5; i++) {
            await page.click('text=Create Task');
            await page.fill('input[name="description"]', `Tarefa ${i}`);
            await page.selectOption('select[name="type"]', 'DAILY');
            await page.selectOption('select[name="weight"]', 'MEDIUM');
            await page.click('button:has-text("Create Task")');
            await page.waitForTimeout(500); // Wait for task to be created
        }

        // Try to create 6th task - should show error
        await page.click('text=Create Task');
        await page.fill('input[name="description"]', 'Tarefa 6');
        await page.selectOption('select[name="type"]', 'DAILY');
        await page.selectOption('select[name="weight"]', 'LOW');
        await page.click('button:has-text("Create Task")');

        // Verify error toast appears
        await expect(page.locator('text=Limite de tarefas recorrentes atingido')).toBeVisible({ timeout: 5000 });
        await expect(page.locator('text=Upgrade')).toBeVisible();
    });

    test('should block access to gift card store for free users', async ({ page }) => {
        // Setup: Register user
        await page.goto('/register');
        await page.fill('input[name="name"]', 'Pai Free Store');
        await page.fill('input[name="email"]', 'free-store@test.com');
        await page.click('button:has-text("Registrar")');
        await page.waitForURL('**/dashboard');

        // Try to access gift card store
        await page.click('text=Loja de Recompensas');

        // Verify access is blocked
        await expect(page.locator('text=Acesso Negado')).toBeVisible();
        await expect(page.locator('text=Gift Card store is only available for Premium users')).toBeVisible();
    });
});
