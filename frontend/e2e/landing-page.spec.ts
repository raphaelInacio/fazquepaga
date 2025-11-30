import { test, expect } from '@playwright/test';

test('Landing Page Login Flow', async ({ page }) => {
    await page.goto('/');

    // Check if "Entrar" button exists and click it
    const enterButton = page.getByRole('button', { name: 'Entrar' }).first();
    await expect(enterButton).toBeVisible();
    await enterButton.click();

    // Verify Modal appears
    await expect(page.getByText('Como você quer entrar?')).toBeVisible();

    // Test "Sou Pai/Mãe" navigation
    const parentButton = page.getByRole('button', { name: 'Sou Pai/Mãe' });
    await expect(parentButton).toBeVisible();

    // Mock auth to allow access to dashboard
    await page.evaluate(() => {
        localStorage.setItem('parentId', 'test-parent-id');
        localStorage.setItem('parentName', 'Test Parent');
    });

    await parentButton.click();
    await expect(page).toHaveURL('/dashboard');

    // Go back and test "Sou Filho(a)" navigation
    await page.goto('/');
    await enterButton.click();

    const childButton = page.getByRole('button', { name: 'Sou Filho(a)' });
    await expect(childButton).toBeVisible();
    await childButton.click();
    await expect(page).toHaveURL('/child-login');
});
