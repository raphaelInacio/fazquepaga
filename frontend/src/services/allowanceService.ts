import api from '@/lib/api';

export const allowanceService = {
    getPredictedAllowance: async (childId: string) => {
        const response = await api.get(`/api/v1/allowance/predicted?child_id=${childId}`);
        return response.data;
    }
};
