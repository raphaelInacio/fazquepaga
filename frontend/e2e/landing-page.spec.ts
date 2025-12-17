import { test, expect } from '@playwright/test';

test('Landing Page Login Flow', async ({ page }) => {
    // Force desktop viewport to ensure consistent rendering
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.goto('/');

    // Check if "Entrar" button exists and click it
    const enterButton = page.locator('button:has-text("Entrar"):visible');
    await expect(enterButton).toBeVisible();
    await enterButton.click();

    // Verify Modal appears and click "Sou Pai/Mãe"
    await expect(page.getByText('Como você quer entrar?')).toBeVisible();
    const parentButton = page.getByRole('button', { name: 'Sou Pai/Mãe' });
    await expect(parentButton).toBeVisible();
    await parentButton.click();

    // Verify redirection to Login page
    await expect(page).toHaveURL(/.*\/login/);

    // Fill Login Form
    await page.fill('input[id="email"]', 'test@example.com');
    await page.fill('input[id="password"]', 'password123');

    // Mock the API call to /api/v1/auth/login to return success
    await page.route('**/api/v1/auth/login', async route => {
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
                token: 'fake-jwt-token',
                user: {
                    id: 'test-parent-id',
                    name: 'Test Parent',
                    email: 'test@example.com',
                    role: 'PARENT'
                }
            })
        });
    });

    await page.click('button[type="submit"]');

    await page.waitForURL('**/dashboard');
});
