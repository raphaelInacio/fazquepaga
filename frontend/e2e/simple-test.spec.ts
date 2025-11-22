import { test, expect } from '@playwright/test';

test.describe('Free User Flow - Simple', () => {
    test('should load homepage and navigate to register', async ({ page }) => {
        // Navigate to homepage
        await page.goto('/');

        // Verify homepage loaded
        await expect(page).toHaveTitle(/FazQuePaga/);

        // Navigate to register page directly
        await page.goto('/register');

        // Verify register page loaded
        await expect(page.locator('input[name="name"]')).toBeVisible();
        await expect(page.locator('input[name="email"]')).toBeVisible();
    });

    test('should register a free user successfully', async ({ page }) => {
        // Go directly to register page
        await page.goto('/register');

        // Fill registration form
        await page.fill('input[name="name"]', 'Pai Free E2E Test');
        await page.fill('input[name="email"]', `free-e2e-${Date.now()}@test.com`);

        // Submit using testid
        await page.click('[data-testid="register-submit-button"]');

        // Wait for redirect to dashboard
        await page.waitForURL('**/dashboard', { timeout: 10000 });

        // Verify dashboard loaded
        await expect(page.locator('text=Dashboard')).toBeVisible();
    });
});
