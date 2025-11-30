import api from '../lib/api';

export interface Transaction {
    id: string;
    childId: string;
    amount: number;
    description: string;
    date: string;
    type: 'CREDIT' | 'DEBIT';
}

export interface LedgerResponse {
    transactions: Transaction[];
    balance: number;
}

export const getLedger = async (childId: string, parentId: string): Promise<LedgerResponse> => {
    const response = await api.get(`/children/${childId}/ledger?parent_id=${parentId}`);
    return response.data;
};

export const getLedgerInsights = async (childId: string, parentId: string): Promise<string> => {
    const response = await api.get<{ insight: string }>(`/children/${childId}/ledger/insights?parent_id=${parentId}`);
    return response.data.insight;
};
