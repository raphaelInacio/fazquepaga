import { test, expect } from '@playwright/test';

test.describe('Subscription Cancellation', () => {
    test('should allow premium user to start cancel flow', async ({ page }) => {
        // Intercept API call for cancellation
        await page.route('**/api/v1/subscription/cancel', async route => {
            const json = {
                status: 'PENDING_CANCELLATION',
                cancellationDate: new Date().toISOString(),
                message: 'Assinatura cancelada com sucesso'
            };
            await route.fulfill({ json });
        });

        // Mock register API call
        await page.route('**/api/v1/auth/register', async route => {
            const json = {
                id: 'user123',
                name: 'Cancel User',
                email: 'cancel-user@test.com',
                phoneNumber: '11999999999',
                subscriptionTier: 'FREE'
            };
            await route.fulfill({ status: 201, json });
        });

        // Mock login API call
        await page.route('**/api/v1/auth/login', async route => {
            const json = {
                token: 'mock-jwt-token',
                refreshToken: 'mock-refresh-token',
                user: {
                    id: 'user123',
                    name: 'Cancel User',
                    email: 'cancel-user@test.com',
                    phoneNumber: '11999999999',
                    subscriptionTier: 'PREMIUM',
                    subscriptionStatus: 'ACTIVE'
                }
            };
            await route.fulfill({ status: 200, json });
        });

        const email = `cancel-${Date.now()}@test.com`;
        const password = 'password123';

        await page.goto('/register');
        await page.fill('input[name="name"]', 'Cancel User');
        await page.fill('input[name="email"]', email);
        await page.fill('input[name="phoneNumber"]', '11999999999');
        await page.fill('input[name="password"]', password);
        await page.fill('input[name="confirmPassword"]', password);
        await page.click('[data-testid="register-submit-button"]');
        await page.waitForURL('**/login', { timeout: 15000 });

        // Realizar o login
        await page.fill('input[type="email"]', email);
        await page.fill('input[type="password"]', password);
        await page.click('button[type="submit"]');
        await page.waitForURL('**/dashboard', { timeout: 15000 });

        // Force Premium state locally
        await page.evaluate(() => {
            const userStr = localStorage.getItem('user');
            if (userStr) {
                const user = JSON.parse(userStr);
                user.subscriptionTier = 'PREMIUM';
                user.subscriptionStatus = 'ACTIVE';
                localStorage.setItem('user', JSON.stringify(user));
            }
        });
        await page.reload();

        await page.goto('/settings');

        // Click cancel
        const cancelBtn = page.locator('[data-testid="cancel-subscription-button"]');
        await expect(cancelBtn).toBeVisible();
        await cancelBtn.click();

        // Step 1: Survey
        await expect(page.locator('text=cancelando?')).toBeVisible();
        
        // Next button should be disabled
        const nextBtn = page.locator('button:has-text("Próximo"), button:has-text("Next")');

        // Select reason
        await page.click('label[for="r1"]');
        
        // Click the next button
        await page.locator('button', { hasText: 'Next' }).or(page.locator('button', { hasText: 'Próximo' })).click();

        // Step 2: Confirmation
        await expect(page.locator('text=Tem certeza?').or(page.locator('text=Are you sure?'))).toBeVisible();

        // Confirm
        await page.locator('button', { hasText: 'Cancelamento' }).or(page.locator('button', { hasText: 'Cancellation' })).click();

        // Success toast
        await expect(page.locator('text=sucesso').or(page.locator('text=success'))).toBeVisible();
    });
});
