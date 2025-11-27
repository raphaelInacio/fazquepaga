import { test, expect } from '@playwright/test';

test.describe('Financial Ledger Flow', () => {
    test('should display the financial ledger with transactions and insights', async ({ page }) => {
        // 1. Register a new user and add a child
        await page.goto('/register');
        await page.fill('input[name="name"]', 'Pai Ledger E2E');
        await page.fill('input[name="email"]', 'ledger-e2e@test.com');
        await page.click('[data-testid="register-submit-button"]');
        await page.waitForURL('**/dashboard');

        await page.click('text=Add Child');
        await page.fill('input[name="name"]', 'Filho Ledger E2E');
        await page.fill('input[name="phoneNumber"]', '+5511999999997');
        await page.fill('input[name="age"]', '12');
        await page.click('button:has-text("Add Child")');
        await page.waitForTimeout(500); // wait for child to be created
        await page.click('text=Filho Ledger E2E');

        // 2. Create a task
        // 2. Create a task
        await page.click('[data-testid="create-task-button"]');
        await page.fill('input[name="description"]', 'Test Task for Ledger');

        await page.click('[data-testid="task-type-select"]');
        await page.getByRole('option', { name: 'One Time' }).click();

        await page.click('[data-testid="task-weight-select"]');
        await page.getByRole('option', { name: 'High' }).click();

        await page.click('[data-testid="create-task-submit-button"]');
        await page.waitForTimeout(500); // wait for task to be created

        // 3. Mark task as completed (by child - not possible in this flow) and then approve it (by parent)
        // For the sake of this test, we will assume the task is completed and just needs approval.
        await page.click('text=Test Task for Ledger');
        await page.click('button:has-text("Approve")');
        await page.waitForTimeout(500); // wait for approval

        // 4. Navigate to the "Financial" tab
        await page.click('text=Financial');

        // 5. Verify that the financial ledger is displayed correctly
        await expect(page.locator('text=Financial Statement')).toBeVisible();
        await expect(page.locator('text=Total Balance:')).toBeVisible();
        await expect(page.locator('text=Test Task for Ledger')).toBeVisible();

        // 6. Verify that the AI insight is displayed
        await expect(page.locator('text=AI Insight')).toBeVisible();
        await expect(page.locator('text=We noticed that your child is saving a lot! Keep up the good work!')).toBeVisible();
    });
});
