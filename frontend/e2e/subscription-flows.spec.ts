import { test, expect } from '@playwright/test';

test.describe('Free vs Premium E2E Tests', () => {

    // Helper function to register a user
    const registerUser = async (page: any, name: string, email: string) => {
        await page.goto('/register');
        await page.fill('input[name="name"]', name);
        await page.fill('input[name="email"]', email);
        await page.click('[data-testid="register-submit-button"]');
        await page.waitForURL('**/dashboard', { timeout: 10000 });
    };

    test.describe('Free User Tests', () => {
        test('should register and access dashboard', async ({ page }) => {
            await registerUser(page, 'Free User', `free-${Date.now()}@test.com`);
            await expect(page.locator('text=Dashboard')).toBeVisible();
        });

        test('should block access to gift card store', async ({ page }) => {
            await registerUser(page, 'Free Store Test', `free-store-${Date.now()}@test.com`);

            // Try to access gift card store
            await page.click('[data-testid="gift-cards-button"]');
            await page.waitForURL('**/gift-cards');

            // Verify access is blocked
            await expect(page.locator('text=Acesso Negado')).toBeVisible();
        });
    });

    test.describe('Premium User Tests', () => {
        test('should allow access to gift card store after upgrade', async ({ page }) => {
            await registerUser(page, 'Premium User', `premium-${Date.now()}@test.com`);

            // Simulate upgrade to Premium
            await page.evaluate(() => {
                const parentData = JSON.parse(localStorage.getItem('parent') || '{}');
                parentData.subscriptionTier = 'PREMIUM';
                parentData.subscriptionStatus = 'ACTIVE';
                localStorage.setItem('parent', JSON.stringify(parentData));
            });

            await page.reload();

            // Access gift card store
            await page.click('[data-testid="gift-cards-button"]');
            await page.waitForURL('**/gift-cards');

            // Verify store loaded (not blocked)
            await expect(page.locator('text=Acesso Negado')).not.toBeVisible();

            // Verify gift cards are visible
            await expect(page.locator('text=Roblox')).toBeVisible({ timeout: 5000 });
        });
    });
});
