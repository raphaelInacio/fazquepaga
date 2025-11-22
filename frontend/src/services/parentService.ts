import api from '@/lib/api';
import { CreateParentRequest } from '@/types';

export const parentService = {
    registerParent: async (data: CreateParentRequest) => {
        const response = await api.post('/api/v1/auth/register', data);
        return response.data;
    },
    // Endpoint removed as it does not exist in the backend API
    // getParent: async (id: string) => { ... }
};
