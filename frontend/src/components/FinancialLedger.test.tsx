import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { FinancialLedger } from './FinancialLedger';
import * as ledgerService from '../services/ledgerService';
import { BrowserRouter as Router } from 'react-router-dom';

// Mock the ledgerService
jest.mock('../services/ledgerService', () => ({
    getLedger: jest.fn(),
    getLedgerInsights: jest.fn(),
}));

describe('FinancialLedger', () => {
    const mockChildId = 'child123';

    beforeEach(() => {
        // Reset mocks before each test
        (ledgerService.getLedger as jest.Mock).mockReset();
        (ledgerService.getLedgerInsights as jest.Mock).mockReset();
    });

    test('renders loading state initially', () => {
        (ledgerService.getLedger as jest.Mock).mockReturnValue(new Promise(() => {})); // Never resolves
        (ledgerService.getLedgerInsights as jest.Mock).mockReturnValue(new Promise(() => {})); // Never resolves

        render(
            <Router>
                <FinancialLedger childId={mockChildId} />
            </Router>
        );
        expect(screen.getByText('Loading...')).toBeInTheDocument();
    });

    test('renders ledger data and insights on successful fetch', async () => {
        const mockLedgerResponse = {
            transactions: [
                { id: '1', childId: mockChildId, amount: 10, description: 'Task 1', date: '2025-01-01T10:00:00Z', type: 'CREDIT' },
                { id: '2', childId: mockChildId, amount: 5, description: 'Snack', date: '2025-01-02T11:00:00Z', type: 'DEBIT' },
            ],
            balance: 5,
        };
        const mockInsights = 'Great job managing your money!';

        (ledgerService.getLedger as jest.Mock).mockResolvedValue(mockLedgerResponse);
        (ledgerService.getLedgerInsights as jest.Mock).mockResolvedValue(mockInsights);

        render(
            <Router>
                <FinancialLedger childId={mockChildId} />
            </Router>
        );

        await waitFor(() => expect(screen.getByText('Financial Statement')).toBeInTheDocument());
        expect(screen.getByText(`Total Balance: $${mockLedgerResponse.balance.toFixed(2)}`)).toBeInTheDocument();
        expect(screen.getByText('AI Insight')).toBeInTheDocument();
        expect(screen.getByText(mockInsights)).toBeInTheDocument();

        expect(screen.getByText('Task 1')).toBeInTheDocument();
        expect(screen.getByText('+$10.00')).toBeInTheDocument();
        expect(screen.getByText('Snack')).toBeInTheDocument();
        expect(screen.getByText('-$5.00')).toBeInTheDocument();
    });

    test('displays error message if ledger fetching fails', async () => {
        (ledgerService.getLedger as jest.Mock).mockRejectedValue(new Error('Network error'));
        (ledgerService.getLedgerInsights as jest.Mock).mockResolvedValue('Some insights');

        render(
            <Router>
                <FinancialLedger childId={mockChildId} />
            </Router>
        );

        await waitFor(() => expect(screen.getByText('Failed to fetch ledger.')).toBeInTheDocument());
        expect(screen.queryByText('Financial Statement')).not.toBeInTheDocument();
    });

    test('renders ledger data even if insights fetching fails', async () => {
        const mockLedgerResponse = {
            transactions: [
                { id: '1', childId: mockChildId, amount: 10, description: 'Task 1', date: '2025-01-01T10:00:00Z', type: 'CREDIT' },
            ],
            balance: 10,
        };

        (ledgerService.getLedger as jest.Mock).mockResolvedValue(mockLedgerResponse);
        (ledgerService.getLedgerInsights as jest.Mock).mockRejectedValue(new Error('AI error'));

        render(
            <Router>
                <FinancialLedger childId={mockChildId} />
            </Router>
        );

        await waitFor(() => expect(screen.getByText('Financial Statement')).toBeInTheDocument());
        expect(screen.getByText(`Total Balance: $${mockLedgerResponse.balance.toFixed(2)}`)).toBeInTheDocument();
        expect(screen.queryByText('AI Insight')).not.toBeInTheDocument(); // Insights should not be displayed
    });
});
