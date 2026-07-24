import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { GiftCardApprovalDialog } from './GiftCardApprovalDialog';
import { GiftCardTransaction } from '@/types';

// Mock translation
jest.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (key: string) => key,
    }),
}));

describe('GiftCardApprovalDialog', () => {
    const mockTransaction: GiftCardTransaction = {
        id: 'tx-123',
        childId: 'child-1',
        parentId: 'parent-1',
        productId: 'prod-1',
        amount: 50.0,
        status: 'PENDING',
        createdAt: new Date().toISOString()
    };

    const mockOnOpenChange = jest.fn();
    const mockOnApprove = jest.fn().mockResolvedValue(undefined);

    beforeEach(() => {
        jest.clearAllMocks();
    });

    it('renders correctly when open', () => {
        render(
            <GiftCardApprovalDialog
                open={true}
                onOpenChange={mockOnOpenChange}
                transaction={mockTransaction}
                childName="Joãozinho"
                onApprove={mockOnApprove}
            />
        );

        expect(screen.getByText('Aprovar Gift Card')).toBeInTheDocument();
        expect(screen.getByText(/Joãozinho solicitou um Gift Card/i)).toBeInTheDocument();
        expect(screen.getByTestId('transaction-amount')).toHaveTextContent('R$ 50.00');
        expect(screen.getByText(/Cobrança no Cartão de Crédito/i)).toBeInTheDocument();
    });

    it('does not render when transaction is null', () => {
        const { container } = render(
            <GiftCardApprovalDialog
                open={true}
                onOpenChange={mockOnOpenChange}
                transaction={null}
                childName="Joãozinho"
                onApprove={mockOnApprove}
            />
        );

        expect(container).toBeEmptyDOMElement();
    });

    it('calls onApprove and closes dialog when approve button is clicked', async () => {
        render(
            <GiftCardApprovalDialog
                open={true}
                onOpenChange={mockOnOpenChange}
                transaction={mockTransaction}
                childName="Joãozinho"
                onApprove={mockOnApprove}
            />
        );

        const approveBtn = screen.getByTestId('approve-gift-card-btn');
        fireEvent.click(approveBtn);

        expect(mockOnApprove).toHaveBeenCalledWith('tx-123');

        await waitFor(() => {
            expect(mockOnOpenChange).toHaveBeenCalledWith(false);
        });
    });

    it('calls onOpenChange with false when cancel is clicked', () => {
        render(
            <GiftCardApprovalDialog
                open={true}
                onOpenChange={mockOnOpenChange}
                transaction={mockTransaction}
                childName="Joãozinho"
                onApprove={mockOnApprove}
            />
        );

        const cancelBtn = screen.getByText('Cancelar');
        fireEvent.click(cancelBtn);

        expect(mockOnOpenChange).toHaveBeenCalledWith(false);
        expect(mockOnApprove).not.toHaveBeenCalled();
    });
});
