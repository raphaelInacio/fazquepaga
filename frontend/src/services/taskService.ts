import api from '@/lib/api';
import { Task, CreateTaskRequest } from '@/types';

export const taskService = {
    createTask: async (childId: string, data: CreateTaskRequest) => {
        const response = await api.post(`/api/v1/tasks?child_id=${childId}`, data);
        return response.data;
    },
    getTasks: async (childId: string): Promise<Task[]> => {
        const response = await api.get(`/api/v1/tasks?child_id=${childId}`);
        return response.data;
    },

    approveTask: async (childId: string, taskId: string, parentId: string): Promise<Task> => {
        const response = await api.post(`/api/v1/tasks/${taskId}/approve?child_id=${childId}&parent_id=${parentId}`);
        return response.data;
    },

    completeTask: async (taskId: string, childId: string): Promise<Task> => {
        const response = await api.post(`/api/v1/tasks/${taskId}/complete?child_id=${childId}`);
        return response.data;
    },

    acknowledgeTask: async (childId: string, taskId: string, parentId: string): Promise<Task> => {
        const response = await api.post(`/api/v1/tasks/${taskId}/acknowledge?child_id=${childId}&parent_id=${parentId}`);
        return response.data;
    },

    rejectTask: async (childId: string, taskId: string, parentId: string): Promise<Task> => {
        const response = await api.post(`/api/v1/tasks/${taskId}/reject?child_id=${childId}&parent_id=${parentId}`);
        return response.data;
    }
};
