import { initializeApp } from 'firebase/app';
import { getAnalytics, isSupported } from 'firebase/analytics';
import { getPerformance } from 'firebase/performance';
import type { Analytics } from 'firebase/analytics';
import type { FirebasePerformance } from 'firebase/performance';

const firebaseConfig = {
    apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
    authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
    projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
    storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
    messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
    appId: import.meta.env.VITE_FIREBASE_APP_ID,
    measurementId: import.meta.env.VITE_FIREBASE_MEASUREMENT_ID,
};

const app = initializeApp(firebaseConfig);

let firebaseAnalytics: Analytics | null = null;
let firebasePerformance: FirebasePerformance | null = null;

isSupported()
    .then((supported) => {
        if (supported) {
            firebaseAnalytics = getAnalytics(app);
        }
    })
    .catch(() => {
        // Analytics not available (e.g., blocked by ad blocker or unsupported browser)
    });

if (typeof window !== 'undefined') {
    try {
        firebasePerformance = getPerformance(app);
    } catch {
        // Performance monitoring not available in this environment
    }
}

export { app, firebaseAnalytics, firebasePerformance };
