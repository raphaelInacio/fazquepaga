
import api from "../lib/api";

export interface SubscriptionStatus {
    tier: "FREE" | "PREMIUM";
    status: "ACTIVE" | "CANCELED" | "PAST_DUE" | "NONE";
    subscriptionId?: string;
    // Trial fields (Jackson removes "is" prefix from boolean getters)
    trialActive: boolean;
    trialDaysRemaining: number | null;
}

export interface SubscribeResponse {
    checkoutUrl: string;
}

export type CancellationReason = 
    | "TOO_EXPENSIVE"
    | "NOT_USING_FEATURES"
    | "FOUND_ALTERNATIVE"
    | "WILL_RETURN_LATER"
    | "OTHER";

export interface CancelSubscriptionRequest {
    reason: CancellationReason;
    reasonDetails?: string;
}

export interface CancelSubscriptionResponse {
    status: string;
    cancellationDate: string;
    message: string;
}

export const subscriptionService = {
    subscribe: async (): Promise<SubscribeResponse> => {
        const response = await api.post<SubscribeResponse>("/api/v1/subscription/subscribe");
        return response.data;
    },

    getStatus: async (): Promise<SubscriptionStatus> => {
        const response = await api.get<SubscriptionStatus>("/api/v1/subscription/status");
        return response.data;
    },

    cancelSubscription: async (data: CancelSubscriptionRequest): Promise<CancelSubscriptionResponse> => {
        const response = await api.post<CancelSubscriptionResponse>("/api/v1/subscription/cancel", data);
        return response.data;
    }
};
