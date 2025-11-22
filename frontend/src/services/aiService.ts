import api from '@/lib/api';

export const aiService = {
    getTaskSuggestions: async (age: number): Promise<string[]> => {
        const response = await api.get(`/api/v1/ai/tasks/suggestions?age=${age}`);
        return response.data;
    }
};
