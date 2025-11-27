import { useEffect, useState } from 'react';
import { getLedger, getLedgerInsights, LedgerResponse } from '../services/ledgerService';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Alert, AlertDescription, AlertTitle } from './ui/alert';
import { Lightbulb } from 'lucide-react';

interface FinancialLedgerProps {
    childId: string;
    parentId: string;
}

export function FinancialLedger({ childId, parentId }: FinancialLedgerProps) {
    const [ledger, setLedger] = useState<LedgerResponse | null>(null);
    const [insights, setInsights] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchLedger = async () => {
            try {
                const ledgerData = await getLedger(childId, parentId);
                setLedger(ledgerData);
            } catch (err) {
                setError('Failed to fetch ledger.');
            }
        };

        const fetchInsights = async () => {
            try {
                const insightsData = await getLedgerInsights(childId, parentId);
                // Only set insights if it's valid text (not HTML)
                if (insightsData && !insightsData.includes('<!doctype') && !insightsData.includes('<html')) {
                    setInsights(insightsData);
                }
            } catch (err) {
                // Not critical, so we can just log it
                console.error('Failed to fetch insights:', err);
            }
        };

        fetchLedger();
        fetchInsights();
    }, [childId]);

    if (error) {
        return <p>{error}</p>;
    }

    if (!ledger) {
        return <p>Loading...</p>;
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle>Financial Statement</CardTitle>
            </CardHeader>
            <CardContent>
                {insights && (
                    <Alert>
                        <Lightbulb className="h-4 w-4" />
                        <AlertTitle>AI Insight</AlertTitle>
                        <AlertDescription>{insights}</AlertDescription>
                    </Alert>
                )}

                <div className="my-4">
                    <h3 className="text-lg font-semibold">Total Balance: ${(ledger.balance ?? 0).toFixed(2)}</h3>
                </div>

                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Date</TableHead>
                            <TableHead>Description</TableHead>
                            <TableHead>Amount</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {(ledger.transactions ?? []).map((transaction) => (
                            <TableRow key={transaction.id}>
                                <TableCell>{new Date(transaction.date).toLocaleDateString()}</TableCell>
                                <TableCell>{transaction.description}</TableCell>
                                <TableCell
                                    className={transaction.type === 'CREDIT' ? 'text-green-500' : 'text-red-500'}
                                >
                                    {transaction.type === 'CREDIT' ? '+' : '-'} ${(transaction.amount ?? 0).toFixed(2)}
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </CardContent>
        </Card>
    );
}
