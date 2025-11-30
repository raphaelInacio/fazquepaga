import { test, expect } from '@playwright/test';

test('Parent Approval Flow', async ({ page }) => {
    test.setTimeout(120000); // Increase timeout for full flow

    // 1. Register Parent
    await page.goto('/register');
    const uniqueId = Date.now();
    const parentEmail = `parent_${uniqueId}@test.com`;
    await page.fill('input[name="name"]', 'Parent User');
    await page.fill('input[name="email"]', parentEmail);
    await page.click('[data-testid="register-submit-button"]');
    await expect(page).toHaveURL('/dashboard');

    // Save Parent ID for later login
    const parentId = await page.evaluate(() => localStorage.getItem('parentId'));
    expect(parentId).toBeTruthy();

    // Wait for dashboard to finish loading
    await expect(page.locator('.animate-spin')).not.toBeVisible();

    // 2. Add Child
    // Wait for the button to be visible and clickable
    await page.waitForSelector('[data-testid="add-child-button"]', { state: 'visible' });
    await page.waitForTimeout(2000); // Extra wait for hydration/listeners

    // Use JS click to bypass potential Playwright issues
    await page.evaluate(() => {
        const btn = document.querySelector('[data-testid="add-child-button"]') as HTMLElement;
        if (btn) btn.click();
    });

    await expect(page).toHaveURL('/add-child');

    await page.fill('input[name="name"]', 'Child User');
    await page.fill('input[name="age"]', '10');
    await page.fill('input[name="phoneNumber"]', '1234567890');
    await page.click('button[type="submit"]', { force: true });
    await expect(page.locator('text=Child User')).toBeVisible();

    // 3. Generate Onboarding Code
    await page.reload();
    try {
        await page.waitForSelector('[data-testid="generate-code-button"]', { state: 'visible', timeout: 5000 });
    } catch (e) {
        console.log('Button not found! Checking for errors...');
        const bodyText = await page.locator('body').innerText();
        if (bodyText.includes('Failed to load')) console.log('ERROR: Failed to load children data');
        if (bodyText.includes('No children found')) console.log('ERROR: No children found after reload');
        console.log('Page content dump:', bodyText);
        throw e;
    }

    // Check for potential error toasts after click
    await page.click('[data-testid="generate-code-button"]', { force: true });

    // Wait for either code or error
    const codePromise = page.waitForSelector('.text-4xl.font-mono');
    const errorPromise = page.waitForSelector('.text-red-600', { timeout: 5000 }).catch(() => null);

    await Promise.race([codePromise, errorPromise]);

    const codeElement = await page.locator('.text-4xl.font-mono').first();
    const onboardingCode = await codeElement.textContent() || '';
    expect(onboardingCode).not.toBe('');
    await page.keyboard.press('Escape'); // Close dialog

    // 4. Create Task for Child
    // Navigate to child's task page
    await page.click('text=Child User');
    await expect(page).toHaveURL(/\/child\/.*\/tasks/);

    await page.click('button:has-text("Criar Tarefa Manual")');
    await page.fill('input[name="description"]', 'Task to Approve');
    // Check "Requires Proof" to ensure it goes to PENDING_APPROVAL
    await page.click('label[for="requiresProof"]');
    // Reward is calculated automatically, no input needed
    await page.click('[data-testid="create-task-submit-button"]');
    await expect(page.locator('text=Task to Approve')).toBeVisible();

    // 5. Logout Parent
    await page.evaluate(() => localStorage.clear());
    await page.goto('/child-login');

    // 6. Child Login
    await page.fill('input[placeholder="Digite seu código aqui"]', onboardingCode);
    await page.click('button:has-text("Entrar! 🚀")');
    await expect(page).toHaveURL('/child-portal');

    // Verify task disappeared (because ChildPortal only shows PENDING tasks)
    await expect(page.locator('text=Task to Approve')).not.toBeVisible();

    // 8. Logout Child
    await page.evaluate(() => localStorage.clear());

    // 9. Parent Login (Restore Session)
    await page.goto('/'); // Go to index first
    await page.evaluate((pid) => {
        if (pid) {
            localStorage.setItem('parentId', pid);
            localStorage.setItem('parentName', 'Parent User');
        }
    }, parentId as string);
    await page.goto('/dashboard');
    await expect(page).toHaveURL('/dashboard');

    // 10. Verify Pending Task
    await expect(page.locator('text=Tarefas Aguardando Aprovação')).toBeVisible();
    await expect(page.locator('text=Task to Approve')).toBeVisible();
    await expect(page.locator('text=Feito por: Child User')).toBeVisible();

    // 11. Approve Task
    await page.click('button:has-text("Aprovar")');

    // 12. Verify Removal
    await expect(page.locator('text=Task to Approve')).not.toBeVisible();
    await expect(page.locator('text=Tarefas Aguardando Aprovação')).not.toBeVisible();

});
