import React, { createContext, useContext, ReactNode } from 'react';
import { useAuth } from '../context/AuthContext';
import { subscriptionService } from '../services/subscriptionService';

interface SubscriptionContextType {
    canCreateTask: (currentRecurringTaskCount: number) => boolean;
    canUseAI: () => boolean;
    canAccessGiftCardStore: () => boolean;
    canAddChild: (currentChildCount: number) => boolean;
    getMaxRecurringTasks: () => number;
    isPremium: () => boolean;
    reloadUser: () => Promise<void>;
}

const SubscriptionContext = createContext<SubscriptionContextType | undefined>(undefined);

const FREE_TIER_MAX_RECURRING_TASKS = 5;
const FREE_TIER_MAX_CHILDREN = 1;

export const SubscriptionProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const { user, updateUser, isAuthenticated } = useAuth();

    const isPremium = (): boolean => {
        return user?.subscriptionTier === 'PREMIUM';
    };

    const canCreateTask = (currentRecurringTaskCount: number): boolean => {
        if (!user || !user.subscriptionTier) return false;
        if (isPremium()) return true;
        return currentRecurringTaskCount < FREE_TIER_MAX_RECURRING_TASKS;
    };

    const canUseAI = (): boolean => {
        return isPremium();
    };

    const canAccessGiftCardStore = (): boolean => {
        return isPremium();
    };

    const canAddChild = (currentChildCount: number): boolean => {
        if (!user || !user.subscriptionTier) return false;
        if (isPremium()) return true;
        return currentChildCount < FREE_TIER_MAX_CHILDREN;
    };

    const getMaxRecurringTasks = (): number => {
        if (!user || !user.subscriptionTier) return 0;
        if (isPremium()) return -1; // Unlimited
        return FREE_TIER_MAX_RECURRING_TASKS;
    };

    const reloadUser = async () => {
        if (!isAuthenticated || !user) return;
        try {
            const status = await subscriptionService.getStatus();
            updateUser({
                ...user,
                subscriptionTier: status.tier,
                subscriptionStatus: status.status
            });
        } catch (error) {
            console.error("Failed to reload subscription status:", error);
        }
    };

    return (
        <SubscriptionContext.Provider
            value={{
                canCreateTask,
                canUseAI,
                canAccessGiftCardStore,
                canAddChild,
                getMaxRecurringTasks,
                isPremium,
                reloadUser,
            }}
        >
            {children}
        </SubscriptionContext.Provider>
    );
};

export const useSubscription = (): SubscriptionContextType => {
    const context = useContext(SubscriptionContext);
    if (!context) {
        throw new Error('useSubscription must be used within a SubscriptionProvider');
    }
    return context;
};
