import { test, expect } from '@playwright/test';

test.describe('Subscription Upgrade Flow', () => {

    test('should allow user to navigate to pricing and start subscription flow', async ({ page }) => {
        // Setup mock for user status (non-premium, trial active)
        await page.route('**/api/v1/subscription/status', async route => {
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                json: {
                    tier: 'FREE',
                    status: 'ACTIVE',
                    subscriptionId: null,
                    trialActive: true,
                    trialDaysRemaining: 3
                }
            });
        });

        // Mock subscribe endpoint to return checkout URL
        await page.route('**/api/v1/subscription/subscribe', async route => {
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                json: {
                    checkoutUrl: 'https://sandbox.asaas.com/checkout/mock-upgrade-session'
                }
            });
        });

        // Mock auth endpoints to prevent hitting real backend
        await page.route('**/api/v1/auth/register', async route => {
            await route.fulfill({
                status: 201,
                contentType: 'application/json',
                json: {
                    id: 'user789',
                    name: 'Upgrade User',
                    email: 'upgrade@test.com',
                    phoneNumber: '11988888888',
                    subscriptionTier: 'FREE'
                }
            });
        });

        await page.route('**/api/v1/auth/login', async route => {
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                json: {
                    token: 'mock-jwt-token',
                    refreshToken: 'mock-refresh-token',
                    user: {
                        id: 'user789',
                        name: 'Upgrade User',
                        email: 'upgrade@test.com',
                        phoneNumber: '11988888888',
                        subscriptionTier: 'FREE',
                        subscriptionStatus: 'ACTIVE'
                    }
                }
            });
        });

        const email = `upgrade-${Date.now()}@test.com`;
        const password = 'password123';

        // 1. Register
        await page.goto('/register');
        await page.fill('input[name="name"]', 'Upgrade User');
        await page.fill('input[name="email"]', email);
        await page.fill('input[name="phoneNumber"]', '11988888888');
        await page.fill('input[name="password"]', password);
        await page.fill('input[name="confirmPassword"]', password);
        await page.click('[data-testid="register-submit-button"]');

        await page.waitForURL('**/login', { timeout: 15000 });

        // 2. Login
        await page.fill('input[type="email"]', email);
        await page.fill('input[type="password"]', password);
        await page.click('button[type="submit"]');
        await page.waitForURL('**/dashboard', { timeout: 15000 });

        // 3. Navigate to subscription page
        await page.goto('/subscription');
        await page.waitForURL('**/subscription');

        // Verify we are on pricing page and see plan information
        await expect(page.locator('h1')).toContainText('Seu Trial: 3 dias restantes');
        await expect(page.locator('text=R$ 9,90')).toBeVisible();

        // Locate upgrade button
        const subscribeBtn = page.getByRole('button', { name: 'Assinar Agora' });
        await expect(subscribeBtn).toBeVisible();

        // Click upgrade button
        await subscribeBtn.click();

        // Wait for page to navigate to the external checkout domain
        await page.waitForURL('**/sandbox.asaas.com/**', { timeout: 15000 });

        // Verify that we are indeed on the Asaas sandbox domain
        expect(page.url()).toContain('sandbox.asaas.com');
    });
});
