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
        mockUseSubscription.mockReturnValue({ isPremium: () => false });

        render(
            <BrowserRouter>
                <PricingPage />
            </BrowserRouter>
        );

        expect(screen.getByText('Upgrade to Premium')).toBeInTheDocument();
        expect(screen.getByText('Free')).toBeInTheDocument();
        expect(screen.getByText('Premium')).toBeInTheDocument();
    });

    test('shows "Active" button when user is premium', () => {
        mockUseSubscription.mockReturnValue({ isPremium: () => true });

        render(
            <BrowserRouter>
                <PricingPage />
            </BrowserRouter>
        );

        const activeButton = screen.getByText('Active');
        expect(activeButton).toBeInTheDocument();
        expect(activeButton).toBeDisabled();
    });

    test('calls subscribe service and redirects on upgrade click', async () => {
        mockUseSubscription.mockReturnValue({ isPremium: () => false });
        mockSubscribe.mockResolvedValue({ checkoutUrl: 'https://asaas.com/checkout' });

        render(
            <BrowserRouter>
                <PricingPage />
            </BrowserRouter>
        );

        const upgradeButton = screen.getByText('Upgrade Now');
        fireEvent.click(upgradeButton);

        expect(mockSubscribe).toHaveBeenCalledTimes(1);

        await waitFor(() => {
            expect(mockNavigateTo).toHaveBeenCalledWith('https://asaas.com/checkout');
        });
    });
});