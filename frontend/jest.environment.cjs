/**
 * Custom Jest environment that configures window.location as a mock-friendly object.
 *
 * JSDOM 30+ marks window.location as non-configurable on the Window prototype,
 * making it impossible to redefine via Object.defineProperty without first
 * deleting the property from the global object directly.
 *
 * Strategy: use `delete` on the global object before redefining, which works
 * because the property is "own" on `this.global` even if non-configurable in
 * the Window prototype — actually we need to use the low-level jsdom API.
 *
 * Alternative: use the `customExportConditions` jsdom trick or simply use
 * Reflect.defineProperty on the prototype. The cleanest approach that works
 * with jest-environment-jsdom@30 is to override `window.location` via
 * `Object.defineProperty` on the prototype if the own property is blocked.
 */
const { TestEnvironment } = require('jest-environment-jsdom');

class CustomTestEnvironment extends TestEnvironment {
    async setup() {
        await super.setup();

        const mockLocation = {
            reload: function () {},
            href: 'http://localhost/',
            pathname: '/',
            search: '',
            hash: '',
            host: 'localhost',
            hostname: 'localhost',
            port: '',
            protocol: 'http:',
            origin: 'http://localhost',
            assign: function () {},
            replace: function () {},
            toString: function () { return 'http://localhost/'; },
        };

        // JSDOM exposes window.location on the Window prototype as non-configurable.
        // We need to bypass this by using Reflect on the actual global object instance.
        // The key insight: `this.global` IS window, but the property is defined on
        // Window.prototype. We can shadow it with an own property using delete + defineProperty
        // on the global object's own prototype chain stop.

        try {
            // Try deleting the own property first (may not exist as own property)
            // eslint-disable-next-line @typescript-eslint/no-dynamic-delete
            delete this.global.location;
        } catch (_) {
            // Ignore — property might not be deletable
        }

        try {
            Object.defineProperty(this.global, 'location', {
                value: mockLocation,
                writable: true,
                configurable: true,
            });
        } catch (_) {
            // Fallback: assign directly if defineProperty is blocked
            this.global.location = mockLocation;
        }
    }
}

module.exports = CustomTestEnvironment;
