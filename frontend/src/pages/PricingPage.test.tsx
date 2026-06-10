import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import PricingPage from './PricingPage';
import { subscriptionService } from '@/services/subscriptionService';
import { useSubscription } from '@/contexts/SubscriptionContext';
import { BrowserRouter } from 'react-router-dom';
import * as utils from '@/lib/utils';

// Mock SubscriptionService
jest.mock('@/services/subscriptionService', () => ({
    subscriptionService: {
        subscribe: jest.fn(),
    },
}));

// Mock SubscriptionContext
jest.mock('@/contexts/SubscriptionContext', () => ({
    useSubscription: jest.fn(),
}));

// Mock utils
jest.mock('@/lib/utils', () => ({
    ...jest.requireActual('@/lib/utils'),
    navigateTo: jest.fn(),
}));

describe('PricingPage', () => {
    const mockSubscribe = subscriptionService.subscribe as jest.Mock;
    const mockUseSubscription = useSubscription as jest.Mock;
    const mockNavigateTo = utils.navigateTo as jest.Mock;

    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('renders pricing plans correctly', () => {
        mockUseSubscription.mockReturnValue({
            isPremium: () => false,
            isTrialActive: () => false,
            trialDaysRemaining: null,
            subscriptionStatus: 'NONE',
        });

        render(
            <BrowserRouter>
                <PricingPage />
            </BrowserRouter>
        );

        expect(screen.getByText('Assine o Premium')).toBeInTheDocument();
        expect(screen.getByText('Premium')).toBeInTheDocument();
        expect(screen.getByText('Assinar Agora')).toBeInTheDocument();
    });

    test('renders pricing plans correctly when trial is active', () => {
        mockUseSubscription.mockReturnValue({
            isPremium: () => false,
            isTrialActive: () => true,
            trialDaysRemaining: 3,
            subscriptionStatus: 'NONE',
        });

        render(
            <BrowserRouter>
                <PricingPage />
            </BrowserRouter>
        );

        expect(screen.getByText('Seu Trial: 3 dias restantes')).toBeInTheDocument();
        expect(screen.getByText('Premium')).toBeInTheDocument();
        expect(screen.getByText('Assinar Agora')).toBeInTheDocument();
        expect(screen.getByText('🎁 Seu trial está ativo! Experimente tudo grátis.')).toBeInTheDocument();
    });

    test('shows "✓ Plano Ativo" button when user is premium and active', () => {
        mockUseSubscription.mockReturnValue({
            isPremium: () => true,
            isTrialActive: () => false,
            trialDaysRemaining: null,
            subscriptionStatus: 'ACTIVE',
        });

        render(
            <BrowserRouter>
                <PricingPage />
            </BrowserRouter>
        );

        const activeButton = screen.getByText('✓ Plano Ativo');
        expect(activeButton).toBeInTheDocument();
        expect(activeButton).toBeDisabled();
    });

    test('shows "Assinar Novamente" button and allows resubscribing when user is premium but pending cancellation', async () => {
        mockUseSubscription.mockReturnValue({
            isPremium: () => true,
            isTrialActive: () => false,
            trialDaysRemaining: null,
            subscriptionStatus: 'PENDING_CANCELLATION',
        });
        mockSubscribe.mockResolvedValue({ checkoutUrl: 'https://asaas.com/checkout' });

        render(
            <BrowserRouter>
                <PricingPage />
            </BrowserRouter>
        );

        const resubscribeButton = screen.getByText('Assinar Novamente');
        expect(resubscribeButton).toBeInTheDocument();
        expect(resubscribeButton).not.toBeDisabled();

        fireEvent.click(resubscribeButton);

        expect(mockSubscribe).toHaveBeenCalledTimes(1);

        await waitFor(() => {
            expect(mockNavigateTo).toHaveBeenCalledWith('https://asaas.com/checkout');
        });
    });

    test('calls subscribe service and redirects on upgrade click', async () => {
        mockUseSubscription.mockReturnValue({
            isPremium: () => false,
            isTrialActive: () => false,
            trialDaysRemaining: null,
            subscriptionStatus: 'NONE',
        });
        mockSubscribe.mockResolvedValue({ checkoutUrl: 'https://asaas.com/checkout' });

        render(
            <BrowserRouter>
                <PricingPage />
            </BrowserRouter>
        );

        const upgradeButton = screen.getByText('Assinar Agora');
        fireEvent.click(upgradeButton);

        expect(mockSubscribe).toHaveBeenCalledTimes(1);

        await waitFor(() => {
            expect(mockNavigateTo).toHaveBeenCalledWith('https://asaas.com/checkout');
        });
    });

    test('calls subscribe service and redirects on upgrade click when trial is active', async () => {
        mockUseSubscription.mockReturnValue({
            isPremium: () => false,
            isTrialActive: () => true,
            trialDaysRemaining: 3,
            subscriptionStatus: 'NONE',
        });
        mockSubscribe.mockResolvedValue({ checkoutUrl: 'https://asaas.com/checkout' });

        render(
            <BrowserRouter>
                <PricingPage />
            </BrowserRouter>
        );

        const upgradeButton = screen.getByText('Assinar Agora');
        fireEvent.click(upgradeButton);

        expect(mockSubscribe).toHaveBeenCalledTimes(1);

        await waitFor(() => {
            expect(mockNavigateTo).toHaveBeenCalledWith('https://asaas.com/checkout');
        });
    });
});