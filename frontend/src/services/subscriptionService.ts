
import api from "../lib/api";

export interface SubscriptionStatus {
    tier: "FREE" | "PREMIUM";
    status: "ACTIVE" | "CANCELED" | "PAST_DUE" | "NONE";
}

export interface SubscribeResponse {
    checkoutUrl: string;
}

export const subscriptionService = {
    subscribe: async (): Promise<SubscribeResponse> => {
        const response = await api.post<SubscribeResponse>("/api/v1/subscription/subscribe");
        return response.data;
    },

    getStatus: async (): Promise<SubscriptionStatus> => {
        const response = await api.get<SubscriptionStatus>("/api/v1/subscription/status");
        return response.data;
    }
};
