import api from '@/lib/api';
import { Task, CreateTaskRequest } from '@/types';

export const taskService = {
    createTask: async (childId: string, data: CreateTaskRequest) => {
        const response = await api.post(`/api/v1/tasks?child_id=${childId}`, data);
        return response.data;
    },
    getTasks: async (childId: string) => {
        const response = await api.get(`/api/v1/tasks?child_id=${childId}`);
        return response.data;
    },
    // Endpoint removed as it does not exist in the backend API
    // approveTask: async (taskId: string) => { ... }
};
