/// <reference types="node" />
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
    testDir: './e2e',
    fullyParallel: true,
    forbidOnly: !!(process.env as any).CI,
    retries: (process.env as any).CI ? 2 : 0,
    workers: (process.env as any).CI ? 1 : undefined,
    reporter: 'html',

    use: {
        baseURL: 'http://localhost:8084',
        trace: 'on-first-retry',
        screenshot: 'only-on-failure',
    },

    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'] },
        },
    ],

    webServer: {
        command: 'npm run dev',
        url: 'http://localhost:8084',
        reuseExistingServer: !(process.env as any).CI,
        timeout: 120 * 1000,
    },
});
