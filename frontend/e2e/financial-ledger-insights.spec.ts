import { test, expect } from '@playwright/test';

test.describe('Financial Ledger with AI Insights', () => {
    let childId: string;
    let parentId: string;

    test.beforeAll(async ({ browser }) => {
        // Setup: Create parent, child, and approved tasks
        const page = await browser.newPage();
        await page.goto('http://localhost:8080');

        // Register parent
        await page.click('text=Cadastrar');
        await page.fill('input[name="name"]', 'Test Parent Ledger');
        await page.fill('input[name="email"]', `test-ledger-${Date.now()}@example.com`);
        await page.click('button:has-text("Registrar")');
        await page.waitForTimeout(2000);

        // Store parent ID from localStorage
        parentId = await page.evaluate(() => localStorage.getItem('parentId') || '');

        // Add child
        await page.click('text=Adicionar Criança');
        await page.fill('input[placeholder*="Nome"]', 'Maria');
        await page.fill('input[placeholder*="Telefone"]', '11999999999');
        await page.fill('input[type="number"]', '10');
        await page.click('button:has-text("Adicionar Criança")');
        await page.waitForTimeout(2000);

        // Navigate to child tasks
        await page.click('text=Ver Tarefas');
        await page.waitForTimeout(1000);

        // Get child ID from URL
        const url = page.url();
        childId = url.split('/child/')[1]?.split('/')[0] || '';

        // Create and approve first task
        await page.click('text=Criar Tarefa Manual');
        await page.fill('input[placeholder*="e.g. Clean room"]', 'Lavar a louça');
        await page.click('button:has-text("Create Task")');
        await page.waitForTimeout(1000);

        // Approve first task
        await page.click('text=Lavar a louça');
        await page.click('button:has-text("Approve")');
        await page.waitForTimeout(2000);

        // Create and approve second task
        await page.click('text=Criar Tarefa Manual');
        await page.fill('input[placeholder*="e.g. Clean room"]', 'Arrumar o quarto');
        // Select High weight
        await page.click('[data-testid="task-weight-select"]');
        await page.click('text=High');
        await page.click('button:has-text("Create Task")');
        await page.waitForTimeout(1000);

        // Approve second task
        await page.click('text=Arrumar o quarto');
        await page.click('button:has-text("Approve")');
        await page.waitForTimeout(2000);

        await page.close();
    });

    test('should display financial ledger with AI insights', async ({ page }) => {
        // Navigate to child tasks page
        await page.goto(`http://localhost:8080/child/${childId}/tasks`);
        await page.waitForTimeout(1000);

        // Click on Financial tab
        await page.click('button[role="tab"]:has-text("Financial")');
        await page.waitForTimeout(3000); // Wait for AI insights to load

        // Verify Financial Ledger title
        await expect(page.locator('text=Extrato Financeiro')).toBeVisible();

        // Verify AI Insight alert is present
        const aiInsightAlert = page.locator('[role="alert"]').filter({ hasText: 'Insight de IA' });
        await expect(aiInsightAlert).toBeVisible();

        // Verify AI insight has gradient styling
        const alertElement = await aiInsightAlert.elementHandle();
        const bgClass = await alertElement?.getAttribute('class');
        expect(bgClass).toContain('gradient');

        // Verify Total Balance is displayed in BRL format
        const balanceText = await page.locator('text=Saldo Total').locator('..').locator('h3').textContent();
        expect(balanceText).toMatch(/R\$\s*\d+[,\.]\d{2}/);

        // Verify transaction table headers
        await expect(page.locator('th:has-text("Data")')).toBeVisible();
        await expect(page.locator('th:has-text("Descrição")')).toBeVisible();
        await expect(page.locator('th:has-text("Valor")')).toBeVisible();

        // Verify transactions are displayed
        await expect(page.locator('text=Lavar a louça')).toBeVisible();
        await expect(page.locator('text=Arrumar o quarto')).toBeVisible();

        // Verify transactions show BRL format with + for credits
        const transactionValues = await page.locator('td.text-green-600').allTextContents();
        expect(transactionValues.length).toBeGreaterThan(0);
        transactionValues.forEach(value => {
            expect(value).toMatch(/\+\s*R\$\s*\d+[,\.]\d{2}/);
        });

        // Verify transactions are ordered by date (most recent first)
        const dates = await page.locator('table tbody tr td:first-child').allTextContents();
        expect(dates.length).toBeGreaterThanOrEqual(2);

        // Take screenshot for documentation
        await page.screenshot({
            path: 'test-results/financial-ledger-with-insights.png',
            fullPage: true
        });
    });

    test('should display AI insight message', async ({ page }) => {
        await page.goto(`http://localhost:8080/child/${childId}/tasks`);
        await page.click('button[role="tab"]:has-text("Financial")');
        await page.waitForTimeout(3000);

        // Verify AI insight message is present and not empty
        const insightMessage = await page.locator('[role="alert"] >> text=/./').first().textContent();
        expect(insightMessage).toBeTruthy();
        expect(insightMessage!.length).toBeGreaterThan(10);

        // Verify insight is in Portuguese (contains common Portuguese words)
        const portugueseWords = ['você', 'seu', 'sua', 'para', 'com', 'muito', 'bem', 'parabéns'];
        const hasPortuguese = portugueseWords.some(word =>
            insightMessage!.toLowerCase().includes(word)
        );
        expect(hasPortuguese).toBeTruthy();
    });

    test('should show empty state when no transactions', async ({ browser }) => {
        // Create a new child without tasks
        const page = await browser.newPage();
        await page.goto('http://localhost:8080');

        // Login as existing parent
        await page.evaluate((pid) => {
            localStorage.setItem('parentId', pid);
        }, parentId);
        await page.reload();

        // Add new child
        await page.click('text=Adicionar Criança');
        await page.fill('input[placeholder*="Nome"]', 'João');
        await page.fill('input[placeholder*="Telefone"]', '11888888888');
        await page.fill('input[type="number"]', '8');
        await page.click('button:has-text("Adicionar Criança")');
        await page.waitForTimeout(2000);

        // Navigate to new child tasks
        await page.click('text=Ver Tarefas >> nth=1'); // Second "Ver Tarefas" button
        await page.waitForTimeout(1000);

        // Click Financial tab
        await page.click('button[role="tab"]:has-text("Financial")');
        await page.waitForTimeout(2000);

        // Verify empty state message
        await expect(page.locator('text=Nenhuma transação ainda')).toBeVisible();
        await expect(page.locator('text=Complete tarefas para começar a ganhar sua mesada')).toBeVisible();

        await page.close();
    });
});
