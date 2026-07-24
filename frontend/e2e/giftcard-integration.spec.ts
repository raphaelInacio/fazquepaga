import { test, expect, Page } from '@playwright/test';

const TIMEOUT_WAIT_URL = 15000;
const TIMEOUT_WAIT_LOCATOR = 5000;

interface ChildMockData {
    childId: string;
    parentId: string;
    accessCode: string;
}

interface ParentMockData {
    parentId: string;
    parentEmail: string;
    transactionId: string;
}

async function setupChildMocks(page: Page, data: ChildMockData) {
    await page.route('**/api/v1/children/login', async route => {
        await route.fulfill({
            status: 200,
            json: {
                token: 'mock-child-jwt-token',
                child: {
                    id: data.childId,
                    name: 'Child E2E',
                    balance: 150.0,
                    monthlyAllowance: 100.0,
                    age: 10,
                    parentId: data.parentId
                },
                message: 'Login success'
            }
        });
    });
    await page.route('**/api/v1/giftcards/catalog', async route => {
        await route.fulfill({
            status: 200,
            json: [
                {
                    productId: 'roblox-50',
                    name: 'Roblox R$ 50',
                    amount: 50.0,
                    category: 'Games',
                    imageUrl: 'https://example.com/roblox.png'
                }
            ]
        });
    });
}

async function setupParentMocks(page: Page, data: ParentMockData, isFailureScenario: boolean) {
    await page.route('**/api/v1/auth/login', async route => {
        await route.fulfill({
            status: 200,
            json: {
                token: 'mock-parent-jwt-token',
                user: {
                    id: data.parentId,
                    name: 'Parent E2E',
                    email: data.parentEmail,
                    subscriptionTier: 'PREMIUM',
                    subscriptionStatus: 'ACTIVE'
                }
            }
        });
    });
    let isApproved = false;
    await page.route('**/api/v1/giftcards/requests*', async route => {
        if (!isApproved) {
             await route.fulfill({
                status: 200,
                json: [{
                    id: data.transactionId,
                    childId: 'child-123',
                    childName: 'Child E2E',
                    parentId: data.parentId,
                    productId: 'roblox-50',
                    amount: 50.0,
                    status: 'PENDING'
                }]
            });
        } else {
             await route.fulfill({ status: 200, json: [] });
        }
    });
    await page.route(`**/api/v1/giftcards/requests/${data.transactionId}/approve`, async route => {
        if (isFailureScenario) {
            await route.fulfill({
                status: 400,
                json: { error: 'Payment Failed', message: 'Cartão recusado pelo emissor.' }
            });
        } else {
            isApproved = true;
            await route.fulfill({
                status: 200,
                json: {
                    id: data.transactionId,
                    status: 'APPROVED',
                    pinCode: 'PIN-12345-67890'
                }
            });
        }
    });
}

test.describe('Gift Card Integration E2E', () => {

    test('Full flow: Child requests Gift Card and Parent approves', async ({ page }) => {
        const childData: ChildMockData = {
            accessCode: 'CODE12',
            childId: 'child-123',
            parentId: 'parent-123'
        };
        await setupChildMocks(page, childData);
        let requestPayload: Record<string, unknown> | null = null;
        await page.route('**/api/v1/giftcards/requests', async route => {
            if (route.request().method() === 'POST') {
                requestPayload = route.request().postDataJSON() as Record<string, unknown>;
                await route.fulfill({
                    status: 201,
                    json: {
                        id: 'tx-456',
                        childId: childData.childId,
                        parentId: childData.parentId,
                        productId: requestPayload?.productId,
                        amount: requestPayload?.amount,
                        status: 'PENDING',
                        createdAt: new Date().toISOString()
                    }
                });
            } else if (route.request().method() === 'GET') {
                if (requestPayload) {
                    await route.fulfill({
                        status: 200,
                        json: [{
                            id: 'tx-456',
                            childId: childData.childId,
                            parentId: childData.parentId,
                            productId: 'roblox-50',
                            amount: 50.0,
                            status: 'PENDING'
                        }]
                    });
                } else {
                    await route.fulfill({ status: 200, json: [] });
                }
            }
        });
        await page.goto('/child-login');
        await page.getByRole('textbox').fill(childData.accessCode);
        await page.click('button:has-text("Entrar! 🚀")');
        await page.waitForURL('**/child-portal', { timeout: TIMEOUT_WAIT_URL });
        await expect(page.locator('h1')).toContainText('Child E2E');
        const storeBtn = page.locator('text=Loja').or(page.locator('text=Gift Cards'));
        if (await storeBtn.isVisible()) {
            await storeBtn.click();
        } else {
            await page.goto('/child-portal/giftcards');
        }
        await expect(page.locator('text=Roblox R$ 50')).toBeVisible({ timeout: TIMEOUT_WAIT_LOCATOR });
        await page.click('button:has-text("Comprar")');
        await page.click('button:has-text("Solicitar")');
        await expect(page.locator('text=Solicitação enviada')).toBeVisible({ timeout: TIMEOUT_WAIT_LOCATOR });
        expect(requestPayload).toBeTruthy();
        expect(requestPayload?.productId).toBe('roblox-50');
        expect(requestPayload?.amount).toBe(50.0);
        await page.evaluate(() => localStorage.clear());
        const parentData: ParentMockData = {
            parentId: 'parent-123',
            parentEmail: 'parent.giftcard@test.com',
            transactionId: 'tx-456'
        };
        await setupParentMocks(page, parentData, false);
        await page.goto('/login');
        await page.fill('input[type="email"]', parentData.parentEmail);
        await page.fill('input[type="password"]', 'password123');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/dashboard', { timeout: TIMEOUT_WAIT_URL });
        await expect(page.locator('text=Roblox R$ 50').first()).toBeVisible({ timeout: TIMEOUT_WAIT_LOCATOR });
        await expect(page.locator('text=Child E2E').first()).toBeVisible({ timeout: TIMEOUT_WAIT_LOCATOR });
        await page.click('button:has-text("Aprovar")');
        await expect(page.locator('text=Esta ação efetuará uma cobrança')).toBeVisible({ timeout: TIMEOUT_WAIT_LOCATOR });
        await page.click('button:has-text("Confirmar")');
        await expect(page.locator('text=sucesso')).toBeVisible({ timeout: TIMEOUT_WAIT_LOCATOR });
        await expect(page.locator('text=Roblox R$ 50')).not.toBeVisible({ timeout: TIMEOUT_WAIT_LOCATOR });
    });

    test('Negative flow: Parent approves but API returns failure', async ({ page }) => {
        const parentData: ParentMockData = {
            parentId: 'parent-123',
            parentEmail: 'parent.failure@test.com',
            transactionId: 'tx-456'
        };
        await setupParentMocks(page, parentData, true);
        await page.goto('/login');
        await page.fill('input[type="email"]', parentData.parentEmail);
        await page.fill('input[type="password"]', 'password123');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/dashboard', { timeout: TIMEOUT_WAIT_URL });
        await expect(page.locator('text=Roblox R$ 50').first()).toBeVisible({ timeout: TIMEOUT_WAIT_LOCATOR });
        await page.click('button:has-text("Aprovar")');
        await expect(page.locator('text=Esta ação efetuará uma cobrança')).toBeVisible({ timeout: TIMEOUT_WAIT_LOCATOR });
        await page.click('button:has-text("Confirmar")');
        await expect(page.locator('text=Cartão recusado pelo emissor.')).toBeVisible({ timeout: TIMEOUT_WAIT_LOCATOR });
        await expect(page.locator('text=Roblox R$ 50').first()).toBeVisible({ timeout: TIMEOUT_WAIT_LOCATOR });
    });
});
