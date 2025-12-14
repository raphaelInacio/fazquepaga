import axios from 'axios';
import i18n from '@/i18n';

const api = axios.create({
    // baseURL: 'http://localhost:8080', // Using proxy in vite.config.ts
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add request interceptor to include Accept-Language header
api.interceptors.request.use((config) => {
    const language = i18n.language || 'pt';
    config.headers['Accept-Language'] = language;

    // Add Authorization header if token exists
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    // Dev auth bypass: Inject user email from localStorage (Legacy/Fallback)
    const parentStr = localStorage.getItem("parent");
    if (parentStr && !token) {
        try {
            const parent = JSON.parse(parentStr);
            if (parent.email) {
                config.headers['X-User-Email'] = parent.email;
            }
        } catch (e) {
            console.error("Failed to parse parent from localStorage", e);
        }
    }

    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        // Enhanced error logging
        if (import.meta.env.DEV) {
            console.error('API Error Details:', {
                url: error.config?.url,
                method: error.config?.method,
                status: error.response?.status,
                data: error.response?.data,
                message: error.message,
            });
        }

        // User-friendly error messages
        const errorMessage = error.response?.data?.message
            || error.response?.data?.error
            || error.message
            || 'An unexpected error occurred';

        // Attach user-friendly message to error
        error.userMessage = errorMessage;

        return Promise.reject(error);
    }
);

export default api;
