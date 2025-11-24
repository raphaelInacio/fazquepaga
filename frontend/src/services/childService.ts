import api from '@/lib/api';
import { CreateChildRequest, User } from '@/types';

export const childService = {
    // Add a new method to get all children for the logged-in parent
    getChildren: async (): Promise<User[]> => {
        const response = await api.get('/api/v1/children');
        return response.data;
    },

    // Add a method to get a single child's details
    getChild: async (childId: string): Promise<User> => {
        const response = await api.get(`/api/v1/children/${childId}`);
        return response.data;
    },

    addChild: async (data: CreateChildRequest) => {
        const response = await api.post('/api/v1/children', data);
        return response.data;
    },

    generateOnboardingCode: async (childId: string) => {
        const response = await api.post(`/api/v1/children/${childId}/onboarding-code`);
        return response.data;
    },

    updateAllowance: async (childId: string, allowance: number) => {
        const response = await api.post(`/api/v1/children/${childId}/allowance`, { allowance });
        return response.data;
    }
};
