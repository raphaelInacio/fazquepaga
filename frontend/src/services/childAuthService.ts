import api from '@/lib/api';

export interface ChildLoginRequest {
    code: string;
    recaptchaToken?: string;
}

export interface ChildLoginResponse {
    child: {
        id: string;
        name: string;
        balance: number;
        monthlyAllowance: number;
        age: number;
        parentId: string;
    };
    message: string;
    token?: string;
    refreshToken?: string;
}

const CHILD_STORAGE_KEY = 'fazquepaga_child';

export const childAuthService = {
    login: async (code: string, recaptchaToken?: string): Promise<ChildLoginResponse> => {
        const response = await api.post('/api/v1/children/login', { code, recaptchaToken });
        const data = response.data;

        // Store child data and token
        localStorage.setItem(CHILD_STORAGE_KEY, JSON.stringify(data.child));
        if (data.token) {
            localStorage.setItem('token', data.token);
        }
        if (data.refreshToken) {
            localStorage.setItem('refreshToken', data.refreshToken);
        }

        return data;
    },

    getCurrentChild: () => {
        const childData = localStorage.getItem(CHILD_STORAGE_KEY);
        return childData ? JSON.parse(childData) : null;
    },

    logout: () => {
        localStorage.removeItem(CHILD_STORAGE_KEY);
    },

    isAuthenticated: (): boolean => {
        return localStorage.getItem(CHILD_STORAGE_KEY) !== null;
    }
};
