import axios from 'axios';
import i18n from '@/i18n';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
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

// Token refresh state
let isRefreshing = false;
let failedQueue: Array<{
    resolve: (value: unknown) => void;
    reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: Error | null, token: string | null = null) => {
    failedQueue.forEach((prom) => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

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

        // Handle 401 with token refresh (skip for refresh endpoint itself)
        if (error.response?.status === 401 && !originalRequest._retry) {
            const refreshToken = localStorage.getItem('refreshToken');

            // Skip refresh for auth endpoints that don't need it
            if (originalRequest.url?.includes('/auth/login') ||
                originalRequest.url?.includes('/auth/register') ||
                originalRequest.url?.includes('/auth/refresh') ||
                !refreshToken) {
                return Promise.reject(error);
            }

            if (isRefreshing) {
                // Queue this request while refresh is in progress
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                }).then((token) => {
                    originalRequest.headers.Authorization = `Bearer ${token}`;
                    return api(originalRequest);
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                const response = await axios.post('/api/v1/auth/refresh', { refreshToken });
                const newToken = response.data.token;

                // Update localStorage
                localStorage.setItem('token', newToken);

                // Update Authorization header for this and queued requests
                originalRequest.headers.Authorization = `Bearer ${newToken}`;
                processQueue(null, newToken);

                return api(originalRequest);
            } catch (refreshError) {
                // Refresh failed - clear auth and redirect to login
                processQueue(refreshError as Error, null);
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('user');

                // Redirect to login
                if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
                    window.location.href = '/login';
                }

                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
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
