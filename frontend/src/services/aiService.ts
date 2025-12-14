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

const mockAiService = {
    getTaskSuggestions: (age: number): string[] => {
        return [
            "Arrumar a cama",
            "Guardar os brinquedos",
            "Ajudar a tirar a mesa",
            "Ler um livro por 15 minutos",
            "Regar as plantas"
        ];
    },
    getGoalCoachPlan: (childId: string, goalDescription: string, targetAmount: number): GoalCoachResponse => {
        return {
            plan: `Legal! Para conseguir seu ${goalDescription} de R$ ${targetAmount}, você precisa juntar dinheiro.\n\nSe você fizer 2 tarefas por dia, em cerca de 2 semanas você consegue! Que tal começar hoje?`,
            imageUrl: ""
        };
    },
    getAdventureTasks: (tasks: any[]): AdventureTask[] => {
        return tasks.map(t => ({
            id: t.id,
            originalDescription: t.description,
            adventureDescription: `⚔️ Quest: ${t.description} do Destino`,
            value: t.value || 0,
            status: t.status
        }));
    }
}

export const aiService = {
    getTaskSuggestions: async (age: number): Promise<string[]> => {
        if (import.meta.env.VITE_ENABLE_AI_MOCK === 'true') {
            console.warn("Using Mock AI Service (Environment Flag)");
            return mockAiService.getTaskSuggestions(age);
        }
        try {
            const response = await api.get(`/api/v1/ai/tasks/suggestions?age=${age}`);
            if (!response.data || response.data.length === 0) throw new Error("Empty AI response");
            return response.data;
        } catch (error) {
            console.warn("AI Service failed, falling back to mock data", error);
            return mockAiService.getTaskSuggestions(age);
        }
    },

    getGoalCoachPlan: async (childId: string, goalDescription: string, targetAmount: number): Promise<GoalCoachResponse> => {
        if (import.meta.env.VITE_ENABLE_AI_MOCK === 'true') {
            console.warn("Using Mock AI Service (Environment Flag)");
            return mockAiService.getGoalCoachPlan(childId, goalDescription, targetAmount);
        }
        try {
            const response = await api.post('/api/v1/ai/goal-coach', {
                childId,
                goalDescription,
                targetAmount
            });
            return response.data;
        } catch (error) {
            console.warn("AI Service failed, falling back to mock data", error);
            return mockAiService.getGoalCoachPlan(childId, goalDescription, targetAmount);
        }
    },

    getAdventureTasks: async (tasks: any[]): Promise<AdventureTask[]> => {
        if (import.meta.env.VITE_ENABLE_AI_MOCK === 'true') {
            console.warn("Using Mock AI Service (Environment Flag)");
            return mockAiService.getAdventureTasks(tasks);
        }
        try {
            const response = await api.post('/api/v1/ai/adventure-mode/tasks', {
                tasks
            });
            return response.data.tasks;
        } catch (error) {
            console.warn("AI Service failed, falling back to mock data", error);
            return mockAiService.getAdventureTasks(tasks);
        }
    }
};
