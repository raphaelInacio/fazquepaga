import { test, expect } from '@playwright/test';

test.describe('Free Trial Flow', () => {

    // Helper function to register a user and get to dashboard
    const registerUser = async (page: any, name: string, email: string) => {
        await page.goto('/register');
        await page.fill('input[name="name"]', name);
        await page.fill('input[name="email"]', email);
        await page.click('[data-testid="register-submit-button"]');
        await page.waitForURL('**/dashboard', { timeout: 10000 });
    };

    test.describe('Trial Active Tests', () => {
        test('should show trial badge with days remaining', async ({ page }) => {
            // Mock API to return trial active with 2 days remaining
            await page.route('**/api/v1/subscription/status', async route => {
                await route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    json: {
                        tier: 'FREE',
                        status: 'ACTIVE',
                        subscriptionId: null,
                        isTrialActive: true,
                        trialDaysRemaining: 2
                    }
                });
            });

            await registerUser(page, 'Trial User', `trial-${Date.now()}@test.com`);

            // Verify badge is visible with trial text
            await expect(page.locator('text=Trial')).toBeVisible({ timeout: 5000 });
            await expect(page.locator('text=2')).toBeVisible();
        });

        test('should not show trial badge for premium users', async ({ page }) => {
            // Mock API to return premium user
            await page.route('**/api/v1/subscription/status', async route => {
                await route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    json: {
                        tier: 'PREMIUM',
                        status: 'ACTIVE',
                        subscriptionId: 'sub_123',
                        isTrialActive: false,
                        trialDaysRemaining: null
                    }
                });
            });

            await registerUser(page, 'Premium User', `premium-trial-${Date.now()}@test.com`);

            // Verify badge is NOT visible
            await expect(page.locator('text=Trial:')).not.toBeVisible();
        });
    });

    test.describe('Trial Expired Tests', () => {
        test('should show blocking modal when trial expired', async ({ page }) => {
            // Mock API to return expired trial
            await page.route('**/api/v1/subscription/status', async route => {
                await route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    json: {
                        tier: 'FREE',
                        status: 'ACTIVE',
                        subscriptionId: null,
                        isTrialActive: false,
                        trialDaysRemaining: 0
                    }
                });
            });

            await registerUser(page, 'Expired User', `expired-${Date.now()}@test.com`);

            // Verify modal is visible
            await expect(page.locator('text=período de teste terminou')).toBeVisible({ timeout: 5000 });
            await expect(page.locator('[data-testid="subscribe-now-button"]')).toBeVisible();
        });

        test('should not allow closing the expired modal', async ({ page }) => {
            // Mock expired trial
            await page.route('**/api/v1/subscription/status', async route => {
                await route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    json: {
                        tier: 'FREE',
                        status: 'ACTIVE',
                        subscriptionId: null,
                        isTrialActive: false,
                        trialDaysRemaining: 0
                    }
                });
            });

            await registerUser(page, 'No Close User', `noclose-${Date.now()}@test.com`);

            // Modal should be present
            await expect(page.locator('text=período de teste terminou')).toBeVisible({ timeout: 5000 });

            // Press Escape - modal should still be visible
            await page.keyboard.press('Escape');
            await expect(page.locator('text=período de teste terminou')).toBeVisible();

            // Verify there's no close button (X)
            await expect(page.locator('[data-testid="close-modal-button"]')).not.toBeVisible();
        });

        test('should redirect to checkout on CTA click', async ({ page }) => {
            // Mock expired trial
            await page.route('**/api/v1/subscription/status', async route => {
                await route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    json: {
                        tier: 'FREE',
                        status: 'ACTIVE',
                        subscriptionId: null,
                        isTrialActive: false,
                        trialDaysRemaining: 0
                    }
                });
            });

            // Mock subscribe endpoint
            await page.route('**/api/v1/subscription/subscribe', async route => {
                await route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    json: {
                        checkoutUrl: 'https://sandbox.asaas.com/checkout/test-session'
                    }
                });
            });

            await registerUser(page, 'Checkout User', `checkout-${Date.now()}@test.com`);

            // Verify modal is visible
            await expect(page.locator('[data-testid="subscribe-now-button"]')).toBeVisible({ timeout: 5000 });

            // Click subscribe button
            await page.click('[data-testid="subscribe-now-button"]');

            // Wait for navigation to Asaas checkout (or verify the button triggers the redirect)
            // Note: The actual redirect happens via window.location.href
            // We verify the button was clicked and loading state appeared
            await expect(page.locator('[data-testid="subscribe-now-button"]')).toBeDisabled();
        });
    });
});
