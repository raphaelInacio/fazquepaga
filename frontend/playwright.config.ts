/// <reference types="node" />
import { defineConfig, devices } from '@playwright/test';

const baseURL = process.env.BASE_URL || 'http://localhost:8082';
const isLocal = baseURL.includes('localhost') || baseURL.includes('127.0.0.1');

export default defineConfig({
    testDir: './e2e',
    fullyParallel: true,
    forbidOnly: !!(process.env as any).CI,
    retries: (process.env as any).CI ? 2 : 0,
    workers: (process.env as any).CI ? 1 : undefined,
    reporter: 'html',
    timeout: 60000,

    use: {
        baseURL,
        trace: 'on-first-retry',
        screenshot: 'only-on-failure',
        locale: 'pt-BR',
    },

    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'] },
        },
    ],

    webServer: isLocal ? {
        command: 'npm run dev',
        url: baseURL,
        reuseExistingServer: true,
        timeout: 120 * 1000,
    } : undefined,
});
