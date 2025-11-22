import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User } from '../types';

interface SubscriptionContextType {
    user: User | null;
    setUser: (user: User | null) => void;
    canCreateTask: (currentRecurringTaskCount: number) => boolean;
    canUseAI: () => boolean;
    canAccessGiftCardStore: () => boolean;
    canAddChild: (currentChildCount: number) => boolean;
    getMaxRecurringTasks: () => number;
    isPremium: () => boolean;
}

const SubscriptionContext = createContext<SubscriptionContextType | undefined>(undefined);

const FREE_TIER_MAX_RECURRING_TASKS = 5;
const FREE_TIER_MAX_CHILDREN = 1;

export const SubscriptionProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);

    // Load user from localStorage on mount
    useEffect(() => {
        const storedParent = localStorage.getItem('parent');
        if (storedParent) {
            try {
                const parentData = JSON.parse(storedParent);
                setUser(parentData);
            } catch (error) {
                console.error('Failed to parse parent data:', error);
            }
        }
    }, []);

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

    return (
        <SubscriptionContext.Provider
            value={{
                user,
                setUser,
                canCreateTask,
                canUseAI,
                canAccessGiftCardStore,
                canAddChild,
                getMaxRecurringTasks,
                isPremium,
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
