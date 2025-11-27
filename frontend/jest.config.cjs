module.exports = {
    testEnvironment: 'jsdom',
    transform: {
        '^.+\\.(ts|tsx)$': ['@swc/jest'],
    },
    moduleNameMapper: {
        '^@/(.*)$': '<rootDir>/src/$1',
    },
    testPathIgnorePatterns: ['/node_modules/', '/e2e/'],
    setupFilesAfterEnv: ['<rootDir>/setupTests.ts'],
};
