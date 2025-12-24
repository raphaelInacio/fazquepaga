import api from '@/lib/api';
import { CreateChildRequest, User } from '@/types';

export const childService = {
    // Add a new method to get all children for the logged-in parent
    getChildren: async (parentId: string): Promise<User[]> => {
        const response = await api.get(`/api/v1/children?parent_id=${parentId}`);
        return response.data;
    },

    // Add a method to get a single child's details
    getChild: async (childId: string, parentId: string): Promise<User> => {
        const response = await api.get(`/api/v1/children/${childId}?parent_id=${parentId}`);
        return response.data;
    },

    addChild: async (data: CreateChildRequest, parentId: string) => {
        const response = await api.post(`/api/v1/children?parent_id=${parentId}`, data);
        return response.data;
    },

    generateOnboardingCode: async (childId: string, parentId: string) => {
        const response = await api.post(`/api/v1/children/${childId}/onboarding-code?parent_id=${parentId}`);
        return response.data;
    },

    updateAllowance: async (childId: string, allowance: number, parentId: string) => {
        const response = await api.post(`/api/v1/children/${childId}/allowance?parent_id=${parentId}`, { allowance });
        return response.data;
    },

    updateChild: async (childId: string, data: Partial<CreateChildRequest>, parentId: string): Promise<User> => {
        const response = await api.put(`/api/v1/children/${childId}?parent_id=${parentId}`, data);
        return response.data;
    },

    deleteChild: async (childId: string, parentId: string): Promise<void> => {
        await api.delete(`/api/v1/children/${childId}?parent_id=${parentId}`);
    },

    requestWithdrawal: async (childId: string, amount: number) => {
        const response = await api.post(`/api/v1/allowance/children/${childId}/withdraw`, { amount });
        return response.data;
    },


    updateAiContext: async (childId: string, context: string, parentId: string): Promise<User> => {
        const response = await api.patch(`/api/v1/children/${childId}/context?parent_id=${parentId}`, { context });
        return response.data;
    }
};

