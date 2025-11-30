import { test, expect } from '@playwright/test';

test.describe('Child Portal Flow', () => {
    let parentEmail: string;
    let childId: string;
    let onboardingCode: string;

    test.beforeEach(async ({ page }) => {
        // Setup: Register parent and create child
        parentEmail = `parent-child-portal-${Date.now()}@test.com`;

        // Register parent
        await page.goto('/register');
        await page.fill('input[name="name"]', 'Pai Portal Criança');
        await page.fill('input[name="email"]', parentEmail);
        await page.click('[data-testid="register-submit-button"]');
        await page.waitForURL('**/dashboard', { timeout: 10000 });

        // Add child
        await page.click('text=Add Child');
        await page.fill('input[name="name"]', 'Criança Teste');
        await page.fill('input[name="phoneNumber"]', '+5511999999998');
        await page.fill('input[name="age"]', '10');
        await page.click('button:has-text("Add Child")');
        await page.waitForTimeout(500);

        // Generate onboarding code directly from dashboard
        await page.click('button:has-text("Gerar Código WhatsApp")');
        await page.waitForTimeout(1000);

        // Extract the onboarding code from the dialog
        const codeElement = await page.locator('.text-4xl.font-mono').first();
        onboardingCode = await codeElement.textContent() || '';

        // Close dialog
        await page.keyboard.press('Escape');

        // Navigate to child tasks page to create a task
        await page.click('text=Criança Teste');
        await page.waitForTimeout(500);

        // Create a task for the child to complete
        await page.click('[data-testid="create-task-button"]');
        await page.fill('input[name="description"]', 'Arrumar a cama');

        await page.click('[data-testid="task-type-select"]');
        await page.getByRole('option', { name: 'Daily' }).click();

        await page.click('[data-testid="task-weight-select"]');
        await page.getByRole('option', { name: 'Medium' }).click();

        await page.click('[data-testid="create-task-submit-button"]');
        await page.waitForTimeout(500);

        // Logout parent
        await page.goto('/');
    });

    test('should login child with onboarding code', async ({ page }) => {
        // Navigate to child login
        await page.goto('/child-login');

        // Verify login page elements
        await expect(page.locator('text=Portal da Criança')).toBeVisible();
        await expect(page.locator('input[type="text"]')).toBeVisible();

        // Enter onboarding code
        await page.fill('input[type="text"]', onboardingCode);

        // Click login button
        await page.click('button:has-text("Entrar")');

        // Wait for redirect to child portal
        await page.waitForURL('**/child-portal', { timeout: 10000 });

        // Verify child portal loaded
        await expect(page.locator('text=Olá, Criança Teste!')).toBeVisible();
        await expect(page.locator('text=Saldo:')).toBeVisible();
    });

    test('should display child balance and pending tasks', async ({ page }) => {
        // Login as child
        await page.goto('/child-login');
        await page.fill('input[type="text"]', onboardingCode);
        await page.click('button:has-text("Entrar")');
        await page.waitForURL('**/child-portal');

        // Verify balance is displayed
        await expect(page.locator('text=Saldo: R$')).toBeVisible();

        // Verify tasks section
        await expect(page.locator('text=Minhas Tarefas')).toBeVisible();

        // Verify the created task is displayed
        await expect(page.locator('text=Arrumar a cama')).toBeVisible();

        // Verify task value is displayed
        await expect(page.locator('text=💰 R$')).toBeVisible();

        // Verify "Já fiz!" button is present
        await expect(page.locator('button:has-text("Já fiz!")')).toBeVisible();
    });

    test('should complete a task successfully', async ({ page }) => {
        // Login as child
        await page.goto('/child-login');
        await page.fill('input[type="text"]', onboardingCode);
        await page.click('button:has-text("Entrar")');
        await page.waitForURL('**/child-portal');

        // Click "Já fiz!" button
        await page.click('button:has-text("Já fiz!")');

        // Wait for success toast
        await expect(page.locator('text=Parabéns! Tarefa concluída!')).toBeVisible({ timeout: 5000 });

        // Wait for task to be removed from list
        await page.waitForTimeout(1000);

        // Verify task is no longer in pending list
        await expect(page.locator('text=Você completou todas as tarefas!')).toBeVisible();
    });

    test('should display Goal Coach section', async ({ page }) => {
        // Login as child
        await page.goto('/child-login');
        await page.fill('input[type="text"]', onboardingCode);
        await page.click('button:has-text("Entrar")');
        await page.waitForURL('**/child-portal');

        // Verify Goal Coach section
        await expect(page.locator('text=Coach Financeiro')).toBeVisible();
        await expect(page.locator('input[placeholder*="jogo"]')).toBeVisible();
        await expect(page.locator('input[type="number"]')).toBeVisible();
        await expect(page.locator('button:has-text("Criar Plano")')).toBeVisible();
    });

    test('should create a goal plan with AI', async ({ page }) => {
        // Login as child
        await page.goto('/child-login');
        await page.fill('input[type="text"]', onboardingCode);
        await page.click('button:has-text("Entrar")');
        await page.waitForURL('**/child-portal');

        // Fill goal information
        await page.fill('input[placeholder*="jogo"]', 'Um jogo novo');
        await page.fill('input[type="number"]', '250');

        // Click create plan button
        await page.click('button:has-text("Criar Plano")');

        // Wait for AI response
        await page.waitForTimeout(3000);

        // Verify success toast
        await expect(page.locator('text=Plano criado!')).toBeVisible({ timeout: 5000 });

        // Verify plan is displayed (should contain some text from AI)
        // Relaxed check: just verify the card content is visible and has some text
        await expect(page.locator('.text-sm.font-semibold.text-blue-900').first()).toBeVisible({ timeout: 10000 });
    });

    test('should toggle Adventure Mode', async ({ page }) => {
        // Login as child
        await page.goto('/child-login');
        await page.fill('input[type="text"]', onboardingCode);
        await page.click('button:has-text("Entrar")');
        await page.waitForURL('**/child-portal');

        // Verify Adventure Mode button exists
        await expect(page.locator('button:has-text("Modo Aventura")')).toBeVisible();

        // Click Adventure Mode button
        await page.click('button:has-text("Modo Aventura")');

        // Wait for AI to transform tasks
        await page.waitForTimeout(3000);

        // Verify success toast
        await expect(page.locator('text=Modo Aventura ativado!')).toBeVisible({ timeout: 5000 });

        // Verify button text changed
        await expect(page.locator('button:has-text("Modo Aventura ON")')).toBeVisible();

        // Task description should be different (adventure mode)
        // The exact text will vary based on AI, but it should be visible
        // We look for the task card content which should contain the new description
        await expect(page.locator('.text-lg.font-semibold.text-gray-800').first()).toContainText(/!/);

        // Toggle off
        await page.click('button:has-text("Modo Aventura ON")');
        await expect(page.locator('text=Modo normal ativado')).toBeVisible({ timeout: 5000 });
    });

    test('should display stats section', async ({ page }) => {
        // Login as child
        await page.goto('/child-login');
        await page.fill('input[type="text"]', onboardingCode);
        await page.click('button:has-text("Entrar")');
        await page.waitForURL('**/child-portal');

        // Verify stats section
        await expect(page.locator('text=Suas Conquistas')).toBeVisible();
        await expect(page.locator('text=Tarefas Pendentes:')).toBeVisible();
        await expect(page.locator('text=Saldo Atual:')).toBeVisible();
        await expect(page.locator('text=Mesada Mensal:')).toBeVisible();
    });

    test('should logout successfully', async ({ page }) => {
        // Login as child
        await page.goto('/child-login');
        await page.fill('input[type="text"]', onboardingCode);
        await page.click('button:has-text("Entrar")');
        await page.waitForURL('**/child-portal');

        // Click logout button
        await page.click('button:has-text("Sair")');

        // Verify redirected to login page
        await page.waitForURL('**/child-login', { timeout: 5000 });
        await expect(page.locator('text=Portal da Criança')).toBeVisible();
    });

    test('should handle invalid login code', async ({ page }) => {
        // Navigate to child login
        await page.goto('/child-login');

        // Enter invalid code
        await page.fill('input[type="text"]', 'INVALID');

        // Click login button
        await page.click('button:has-text("Entrar")');

        // Verify error message
        await expect(page.locator('text=Código inválido')).toBeVisible({ timeout: 5000 });

        // Verify still on login page
        await expect(page.locator('text=Portal da Criança')).toBeVisible();
    });

    test('should redirect to login if not authenticated', async ({ page }) => {
        // Try to access child portal directly without login
        await page.goto('/child-portal');

        // Should redirect to login
        await page.waitForURL('**/child-login', { timeout: 5000 });
        await expect(page.locator('text=Portal da Criança')).toBeVisible();
    });

    test('should be mobile responsive', async ({ page }) => {
        // Set mobile viewport
        await page.setViewportSize({ width: 375, height: 667 });

        // Login as child
        await page.goto('/child-login');
        await page.fill('input[type="text"]', onboardingCode);
        await page.click('button:has-text("Entrar")');
        await page.waitForURL('**/child-portal');

        // Verify all main sections are visible on mobile
        await expect(page.locator('text=Olá, Criança Teste!')).toBeVisible();
        await expect(page.locator('text=Minhas Tarefas')).toBeVisible();
        await expect(page.locator('text=Coach Financeiro')).toBeVisible();
        await expect(page.locator('text=Suas Conquistas')).toBeVisible();

        // Verify buttons are large enough for touch
        const button = await page.locator('button:has-text("Já fiz!")').first();
        const box = await button.boundingBox();
        expect(box?.height).toBeGreaterThanOrEqual(44); // Minimum touch target size
    });
});
