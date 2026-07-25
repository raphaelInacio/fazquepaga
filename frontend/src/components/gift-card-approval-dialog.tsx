import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { AlertTriangle, Gift, Loader2 } from 'lucide-react';
import { GiftCardTransaction } from '@/types';

interface GiftCardApprovalDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    transaction: GiftCardTransaction | null;
    childName: string;
    onApprove: (transactionId: string) => Promise<void>;
}

export const GiftCardApprovalDialog: React.FC<GiftCardApprovalDialogProps> = ({
    open,
    onOpenChange,
    transaction,
    childName,
    onApprove
}) => {
    const { t } = useTranslation();
    const [isApproving, setIsApproving] = useState(false);

    if (!transaction) return null;

    const handleApprove = async () => {
        setIsApproving(true);
        try {
            await onApprove(transaction.id);
            onOpenChange(false);
        } catch (error) {
            console.error("Failed to approve gift card", error);
        } finally {
            setIsApproving(false);
        }
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-md bg-white border border-slate-100 rounded-3xl p-6" data-testid="gift-card-approval-dialog">
                <DialogHeader>
                    <DialogTitle className="text-2xl font-black text-center text-slate-800 flex items-center justify-center gap-2">
                        <Gift className="w-6 h-6 text-purple-600" />
                        Aprovar Gift Card
                    </DialogTitle>
                    <DialogDescription className="text-center font-medium text-slate-500">
                        {childName} solicitou um Gift Card
                    </DialogDescription>
                </DialogHeader>

                <div className="py-6 flex flex-col items-center gap-6">
                    {/* Financial Details */}
                    <div className="w-full bg-slate-50/50 border border-slate-100 rounded-2xl p-4 space-y-3">
                        <div className="flex justify-between text-sm font-medium">
                            <span className="text-slate-500">Dependente:</span>
                            <span className="text-slate-700 font-bold">{childName}</span>
                        </div>
                        <div className="flex justify-between text-sm font-medium">
                            <span className="text-slate-500">Valor do prêmio:</span>
                            <span className="text-slate-700 font-bold" data-testid="transaction-amount">
                                R$ {transaction.amount.toFixed(2)}
                            </span>
                        </div>
                    </div>

                    {/* Alert / Warning */}
                    <div className="flex items-start gap-3 bg-amber-50 border border-amber-200 p-4 rounded-2xl text-amber-800 text-xs shadow-sm">
                        <AlertTriangle className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
                        <div className="space-y-1">
                            <p className="font-bold text-amber-900 text-sm">Cobrança no Cartão de Crédito</p>
                            <p className="leading-relaxed font-medium">
                                Esta ação efetuará uma cobrança de <strong>R$ {transaction.amount.toFixed(2)}</strong> no seu cartão de crédito cadastrado. O saldo do aplicativo de seu filho é apenas educativo e não cobre os custos desta operação.
                            </p>
                        </div>
                    </div>
                </div>

                <DialogFooter className="flex-col sm:flex-row gap-2">
                    <Button
                        variant="outline"
                        className="w-full sm:w-1/2 rounded-xl font-bold"
                        onClick={() => onOpenChange(false)}
                        disabled={isApproving}
                    >
                        Cancelar
                    </Button>
                    <Button
                        className="w-full sm:w-1/2 rounded-xl font-bold bg-green-600 hover:bg-green-700 text-white shadow-md hover:shadow-green-500/20"
                        disabled={isApproving}
                        onClick={handleApprove}
                        data-testid="approve-gift-card-btn"
                    >
                        {isApproving ? (
                            <>
                                <Loader2 className="w-4 h-4 animate-spin mr-2" />
                                Processando...
                            </>
                        ) : (
                            "Aprovar e Pagar"
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};
