import api from "@/lib/api";
import { FamilyStats } from "@/types";

export const statsService = {
    getFamilyStats: async (familyId: string): Promise<FamilyStats> => {
        const response = await api.get(`/api/v1/families/${familyId}/stats`);
        return response.data;
    }
};
