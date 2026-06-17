import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import '@testing-library/jest-dom';
import { BrowserRouter } from 'react-router-dom';
import { GiftCard, GiftCardTransaction } from '@/types';

// Mock Services and Components at the top
jest.mock('@/services/giftCardService', () => ({
    giftCardService: {
        getAvailableGiftCards: jest.fn(),
        requestGiftCard: jest.fn(),
        getGiftCardRequests: jest.fn(),
    },
}));

jest.mock('@/services/childService', () => ({
    childService: {
        getChild: jest.fn(),
    },
}));

jest.mock('@/services/childAuthService', () => ({
    childAuthService: {
        getCurrentChild: jest.fn(),
        isAuthenticated: jest.fn(),
    },
}));

jest.mock('@/contexts/SubscriptionContext', () => ({
    useSubscription: jest.fn(),
}));

jest.mock('@/context/AuthContext', () => ({
    useAuth: jest.fn(),
}));

jest.mock('canvas-confetti', () => jest.fn());

jest.mock('@/components/ui/dialog', () => {
    const actual = jest.requireActual('@/components/ui/dialog');
    return {
        ...actual,
        DialogContent: ({ children, ...props }: { children: React.ReactNode }) => (
            <div data-testid="dialog-content" {...props}>
                {children}
            </div>
        ),
    };
});

// Import component under test after mocks are defined
import { GiftCardStorePage } from './GiftCardStorePage';
import { giftCardService } from '@/services/giftCardService';
import { childService } from '@/services/childService';
import { childAuthService } from '@/services/childAuthService';
import { useSubscription } from '@/contexts/SubscriptionContext';
import { useAuth } from '@/context/AuthContext';

// Mock lucide-react (optional, but handles rendering icons gracefully in tests if needed)

describe('GiftCardStorePage', () => {
    const mockGetAvailableGiftCards = giftCardService.getAvailableGiftCards as jest.Mock;
    const mockRequestGiftCard = giftCardService.requestGiftCard as jest.Mock;
    const mockGetGiftCardRequests = giftCardService.getGiftCardRequests as jest.Mock;
    
    const mockGetChild = childService.getChild as jest.Mock;
    const mockGetCurrentChild = childAuthService.getCurrentChild as jest.Mock;
    const mockIsAuthenticated = childAuthService.isAuthenticated as jest.Mock;

    const mockUseSubscription = useSubscription as jest.Mock;
    const mockUseAuth = useAuth as jest.Mock;

    const mockChildUser = {
        id: 'child-123',
        name: 'Dudu',
        balance: 75.00,
        monthlyAllowance: 50.00,
        parentId: 'parent-456',
        role: 'CHILD'
    };

    const mockCatalog: GiftCard[] = [
        {
            id: '1',
            name: 'Roblox R$50',
            brand: 'Roblox',
            value: 50.00,
            description: '50 Robux para usar no Roblox'
        },
        {
            id: '2',
            name: 'iFood R$30',
            brand: 'iFood',
            value: 30.00,
            description: 'Vale de R$30 para pedir comida'
        }
    ];

    const mockTransactions: GiftCardTransaction[] = [
        {
            id: 'tx-1',
            childId: 'child-123',
            parentId: 'parent-456',
            productId: '3',
            amount: 100.00,
            status: 'COMPLETED',
            pinCode: 'PLAYSTATION-PIN-12345',
            createdAt: new Date().toISOString(),
            idempotencyKey: 'key-1'
        },
        {
            id: 'tx-2',
            childId: 'child-123',
            parentId: 'parent-456',
            productId: '4',
            amount: 25.00,
            status: 'PENDING',
            createdAt: new Date().toISOString(),
            idempotencyKey: 'key-2'
        }
    ];

    beforeEach(() => {
        jest.clearAllMocks();
        
        // Default mocks
        mockIsAuthenticated.mockReturnValue(true);
        mockGetCurrentChild.mockReturnValue(mockChildUser);
        mockGetChild.mockResolvedValue(mockChildUser);
        mockGetAvailableGiftCards.mockResolvedValue(mockCatalog);
        mockGetGiftCardRequests.mockResolvedValue(mockTransactions);

        mockUseSubscription.mockReturnValue({
            canAccessGiftCardStore: () => true,
            isPremium: () => true,
        });
        mockUseAuth.mockReturnValue({
            user: null,
        });
    });

    test('renders available gift cards and requests correctly', async () => {
        render(
            <BrowserRouter>
                <GiftCardStorePage />
            </BrowserRouter>
        );

        // Header and page title
        expect(screen.getByText('giftCardStore.title')).toBeInTheDocument();
        
        // Wait for data loading
        await waitFor(() => {
            expect(mockGetAvailableGiftCards).toHaveBeenCalledWith('child-123');
            expect(mockGetGiftCardRequests).toHaveBeenCalledWith('child-123');
        });

        // Catalog cards
        expect(screen.getByText('Roblox R$50')).toBeInTheDocument();
        expect(screen.getByText('iFood R$30')).toBeInTheDocument();
        
        // Balance card (Premium Club & child balance)
        expect(screen.getByText('giftCardStore.balance')).toBeInTheDocument();
        expect(screen.getByText('R$ 75.00')).toBeInTheDocument();

        // History items
        expect(screen.getByText('PLAYSTATION-PIN-12345')).toBeInTheDocument();
        expect(screen.getByText('giftCardStore.statusPending')).toBeInTheDocument();
    });

    test('opens request confirmation modal on card click and submits successfully', async () => {
        mockRequestGiftCard.mockResolvedValue({
            id: 'tx-new',
            childId: 'child-123',
            parentId: 'parent-456',
            productId: '1',
            amount: 50.00,
            status: 'PENDING',
            createdAt: new Date().toISOString(),
            idempotencyKey: 'key-new'
        });

        render(
            <BrowserRouter>
                <GiftCardStorePage />
            </BrowserRouter>
        );

        await waitFor(() => {
            expect(screen.getByText('Roblox R$50')).toBeInTheDocument();
        });

        // Click on Roblox card to request it (we have R$75, which is enough for R$50)
        const robloxCard = screen.getByTestId('gift-card-1');
        fireEvent.click(robloxCard);

        // Dialog should open
        expect(await screen.findByText('giftCardStore.requestDialogTitle')).toBeInTheDocument();
        
        const dialog = screen.getByTestId('dialog-content');
        expect(within(dialog).getByText('giftCardStore.remainingBalance')).toBeInTheDocument();
        
        // Remaining balance calculation check: R$ 75 - R$ 50 = R$ 25
        expect(within(dialog).getByText('R$ 25.00')).toBeInTheDocument();

        const requestBtn = within(dialog).getByRole('button', { name: /giftCardStore.requestBtn/i });
        expect(requestBtn).not.toBeDisabled();

        // Submit request
        fireEvent.click(requestBtn);

        await waitFor(() => {
            expect(mockRequestGiftCard).toHaveBeenCalledWith('1', 50.00, 'child-123');
        });
    });

    test('disables request button and shows warning if balance is insufficient', async () => {
        // Mock child with insufficient balance (R$ 15.00)
        const lowBalanceChild = { ...mockChildUser, balance: 15.00 };
        mockGetCurrentChild.mockReturnValue(lowBalanceChild);
        mockGetChild.mockResolvedValue(lowBalanceChild);

        render(
            <BrowserRouter>
                <GiftCardStorePage />
            </BrowserRouter>
        );

        await waitFor(() => {
            expect(screen.getByText('Roblox R$50')).toBeInTheDocument();
        });

        // Click on Roblox card (R$ 50)
        const robloxCard = screen.getByTestId('gift-card-1');
        fireEvent.click(robloxCard);

        // Dialog opens
        expect(await screen.findByText('giftCardStore.requestDialogTitle')).toBeInTheDocument();
        
        const dialog = screen.getByTestId('dialog-content');
        expect(within(dialog).getByText('giftCardStore.insufficientFunds')).toBeInTheDocument();
        
        // Remaining balance would be negative: R$ 15 - R$ 50 = - R$ 35
        expect(within(dialog).getByText('R$ -35.00')).toBeInTheDocument();

        // Button should be disabled
        const requestBtn = within(dialog).getByRole('button', { name: /giftCardStore.requestBtn/i });
        expect(requestBtn).toBeDisabled();
    });
});
