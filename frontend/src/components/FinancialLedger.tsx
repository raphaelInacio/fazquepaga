import { useEffect, useState } from 'react';
import { getLedger, getLedgerInsights, LedgerResponse } from '../services/ledgerService';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Alert, AlertDescription, AlertTitle } from './ui/alert';
import { Lightbulb, TrendingUp } from 'lucide-react';

interface FinancialLedgerProps {
    childId: string;
    parentId: string;
}

export function FinancialLedger({ childId, parentId }: FinancialLedgerProps) {
    const [ledger, setLedger] = useState<LedgerResponse | null>(null);
    const [insights, setInsights] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchLedger = async () => {
            try {
                setIsLoading(true);
                const ledgerData = await getLedger(childId, parentId);
                setLedger(ledgerData);
            } catch (err) {
                setError('Falha ao carregar o extrato.');
                console.error(err);
            } finally {
                setIsLoading(false);
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
    }, [childId, parentId]);

    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('pt-BR', {
            style: 'currency',
            currency: 'BRL'
        }).format(amount);
    };

    if (error) {
        return (
            <Card>
                <CardContent className="pt-6">
                    <p className="text-center text-red-500">{error}</p>
                </CardContent>
            </Card>
        );
    }

    if (isLoading) {
        return (
            <Card>
                <CardContent className="pt-6">
                    <div className="space-y-3">
                        <div className="h-4 bg-gray-200 rounded animate-pulse"></div>
                        <div className="h-4 bg-gray-200 rounded animate-pulse w-3/4"></div>
                        <div className="h-4 bg-gray-200 rounded animate-pulse w-1/2"></div>
                    </div>
                </CardContent>
            </Card>
        );
    }

    if (!ledger) {
        return null;
    }

    const sortedTransactions = [...(ledger.transactions ?? [])].sort((a, b) =>
        new Date(b.date).getTime() - new Date(a.date).getTime()
    );

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <TrendingUp className="h-5 w-5 text-green-600" />
                    Extrato Financeiro
                </CardTitle>
                <CardDescription>
                    Acompanhe todas as transações e o saldo atual
                </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
                {insights && (
                    <Alert className="border-2 border-purple-200 bg-gradient-to-br from-purple-50 to-blue-50">
                        <Lightbulb className="h-5 w-5 text-purple-600" />
                        <AlertTitle className="text-purple-900 font-semibold">💡 Insight de IA</AlertTitle>
                        <AlertDescription className="text-purple-800">{insights}</AlertDescription>
                    </Alert>
                )}

                <div className="bg-gradient-to-r from-green-50 to-emerald-50 p-6 rounded-lg border-2 border-green-200">
                    <p className="text-sm text-green-700 font-medium mb-1">Saldo Total</p>
                    <h3 className="text-3xl font-bold text-green-700">
                        {formatCurrency(ledger.balance ?? 0)}
                    </h3>
                </div>

                {sortedTransactions.length === 0 ? (
                    <div className="text-center py-8 text-gray-500">
                        <p className="text-lg font-medium">Nenhuma transação ainda</p>
                        <p className="text-sm mt-2">Complete tarefas para começar a ganhar sua mesada!</p>
                    </div>
                ) : (
                    <div className="rounded-lg border">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Data</TableHead>
                                    <TableHead>Descrição</TableHead>
                                    <TableHead className="text-right">Valor</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {sortedTransactions.map((transaction) => (
                                    <TableRow key={transaction.id}>
                                        <TableCell className="font-medium">
                                            {new Date(transaction.date).toLocaleDateString('pt-BR')}
                                        </TableCell>
                                        <TableCell>{transaction.description}</TableCell>
                                        <TableCell
                                            className={`text-right font-semibold ${transaction.type === 'CREDIT' ? 'text-green-600' : 'text-red-600'
                                                }`}
                                        >
                                            {transaction.type === 'CREDIT' ? '+' : '-'} {formatCurrency(transaction.amount ?? 0)}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
