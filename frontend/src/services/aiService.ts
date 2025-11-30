import api from '@/lib/api';

export interface GoalCoachRequest {
    childId: string;
    goalDescription: string;
    targetAmount: number;
}

export interface GoalCoachResponse {
    plan: string;
    imageUrl?: string;
}

export interface AdventureTask {
    id: string;
    originalDescription: string;
    adventureDescription: string;
    value: number;
    status: string;
}

export const aiService = {
    getTaskSuggestions: async (age: number): Promise<string[]> => {
        const response = await api.get(`/api/v1/ai/tasks/suggestions?age=${age}`);
        return response.data;
    },

    getGoalCoachPlan: async (childId: string, goalDescription: string, targetAmount: number): Promise<GoalCoachResponse> => {
        const response = await api.post('/api/v1/ai/goal-coach', {
            childId,
            goalDescription,
            targetAmount
        });
        return response.data;
    },

    getAdventureTasks: async (tasks: any[]): Promise<AdventureTask[]> => {
        const response = await api.post('/api/v1/ai/adventure-mode/tasks', {
            tasks
        });
        return response.data.tasks;
    }
};
