/// <reference types="node" />
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
    testDir: './e2e',
    fullyParallel: true,
    forbidOnly: !!(process.env as any).CI,
    retries: (process.env as any).CI ? 2 : 0,
    workers: (process.env as any).CI ? 1 : undefined,
    reporter: 'html',
    timeout: 60000,

    use: {
        baseURL: 'http://localhost:8082',
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
        url: 'http://localhost:8082',
        reuseExistingServer: !(process.env as any).CI,
        timeout: 120 * 1000,
        // Add a waitOn option to ensure the server is ready
        // You might need to adjust the pattern based on Vite's actual output
        // stdout: 'ready',
    },
});
