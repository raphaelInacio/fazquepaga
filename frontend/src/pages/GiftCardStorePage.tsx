import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useSubscription } from '../contexts/SubscriptionContext';
import { useAuth } from '../context/AuthContext';
import { childAuthService } from '@/services/childAuthService';
import { childService } from '@/services/childService';
import { giftCardService } from '../services/giftCardService';
import { GiftCard, GiftCardTransaction, User } from '../types';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { toast } from 'sonner';
import confetti from 'canvas-confetti';
import {
    Gift,
    Loader2,
    ShoppingCart,
    Copy,
    Check,
    Clock,
    AlertTriangle,
    ArrowLeft,
    Coins,
    CreditCard,
    Sparkles,
    Gamepad2,
    Utensils,
    HelpCircle,
    XCircle,
    CheckCircle2
} from 'lucide-react';

export const GiftCardStorePage: React.FC = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { canAccessGiftCardStore } = useSubscription();
    const { user: parentUser } = useAuth();
    
    // Sessions
    const [child, setChild] = useState<User | null>(null);
    const [userRole, setUserRole] = useState<'PARENT' | 'CHILD' | null>(null);
    const [userId, setUserId] = useState<string | null>(null);
    
    // Data States
    const [giftCards, setGiftCards] = useState<GiftCard[]>([]);
    const [transactions, setTransactions] = useState<GiftCardTransaction[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    
    // UI States
    const [selectedCard, setSelectedCard] = useState<GiftCard | null>(null);
    const [isRequesting, setIsRequesting] = useState(false);
    const [copiedPinId, setCopiedPinId] = useState<string | null>(null);

    // Initial session loading
    useEffect(() => {
        const currentChild = childAuthService.getCurrentChild();
        if (currentChild) {
            setChild(currentChild);
            setUserRole('CHILD');
            setUserId(currentChild.id);
        } else if (parentUser) {
            setUserRole('PARENT');
            setUserId(parentUser.id);
        } else {
            // Check if there is a parent token, otherwise redirect to login selector
            const token = localStorage.getItem('token');
            if (!token) {
                navigate('/child-login');
            } else {
                setUserRole('PARENT');
            }
        }
    }, [parentUser, navigate]);

    // Load data once session is resolved
    useEffect(() => {
        if (userId) {
            loadData();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [userId]);

    const loadData = async () => {
        if (!userId) return;
        setLoading(true);
        setError(null);
        try {
            // Catalog
            const cards = await giftCardService.getAvailableGiftCards(userId);
            setGiftCards(cards);

            // Transactions history
            const txs = await giftCardService.getGiftCardRequests(userId);
            // Sort by creation date descending
            txs.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
            setTransactions(txs);

            // If child is logged in, refresh child details to get latest balance
            if (userRole === 'CHILD' && child) {
                const updatedChild = await childService.getChild(child.id, child.parentId);
                setChild(updatedChild);
                localStorage.setItem('fazquepaga_child', JSON.stringify(updatedChild));
            }
        } catch (err: unknown) {
            const error = err as { response?: { status?: number }; userMessage?: string };
            console.error(error);
            if (error.response?.status === 402 || error.userMessage?.includes('Premium')) {
                setError(t("giftCardStore.premiumError") || "A loja de Gift Cards está disponível apenas para usuários Premium. Peça ao seu responsável para assinar!");
            } else {
                setError(t("giftCardStore.loadError") || "Não foi possível carregar os Gift Cards.");
            }
        } finally {
            setLoading(false);
        }
    };

    const handleRequestGiftCard = async () => {
        if (!selectedCard || !userId || userRole !== 'CHILD' || !child) return;
        
        setIsRequesting(true);
        try {
            await giftCardService.requestGiftCard(selectedCard.id, selectedCard.value, userId);
            
            // Success feedback
            toast.success(t("giftCardStore.requestSuccess") || "Pedido enviado para o seu responsável! 🎉");
            
            confetti({
                particleCount: 120,
                spread: 70,
                origin: { y: 0.6 },
                colors: ['#A855F7', '#EC4899', '#3B82F6', '#10B981']
            });

            setSelectedCard(null);
            // Refresh data to show pending transaction and update balance
            loadData();
        } catch (err: unknown) {
            const error = err as { userMessage?: string };
            console.error(error);
            toast.error(error.userMessage || t("giftCardStore.requestError") || "Erro ao fazer a solicitação.");
        } finally {
            setIsRequesting(false);
        }
    };

    const copyToClipboard = (pin: string, txId: string) => {
        navigator.clipboard.writeText(pin);
        setCopiedPinId(txId);
        toast.success(t("giftCardStore.pinCopied") || "Código copiado!");
        setTimeout(() => setCopiedPinId(null), 2000);
    };

    // Helper functions for aesthetics
    const getCardStyle = (brand: string) => {
        const b = brand.toLowerCase();
        if (b.includes('roblox')) {
            return {
                bg: 'bg-gradient-to-br from-neutral-900 via-neutral-800 to-red-950',
                text: 'text-white',
                brandText: 'text-red-500',
                badgeBg: 'bg-red-500/20 text-red-300 border-red-500/30'
            };
        }
        if (b.includes('ifood')) {
            return {
                bg: 'bg-gradient-to-br from-red-600 via-rose-600 to-red-900',
                text: 'text-white',
                brandText: 'text-white font-bold',
                badgeBg: 'bg-white/20 text-white border-white/30'
            };
        }
        if (b.includes('playstation')) {
            return {
                bg: 'bg-gradient-to-br from-blue-800 via-blue-700 to-slate-900',
                text: 'text-white',
                brandText: 'text-sky-300',
                badgeBg: 'bg-sky-500/20 text-sky-300 border-sky-500/30'
            };
        }
        return {
            bg: 'bg-gradient-to-br from-purple-700 via-indigo-700 to-pink-900',
            text: 'text-white',
            brandText: 'text-pink-300',
            badgeBg: 'bg-pink-500/20 text-pink-300 border-pink-500/30'
        };
    };

    const getCategoryIcon = (brand: string) => {
        const b = brand.toLowerCase();
        if (b.includes('roblox') || b.includes('playstation') || b.includes('nintendo') || b.includes('xbox')) {
            return <Gamepad2 className="w-5 h-5 text-indigo-400" />;
        }
        if (b.includes('ifood') || b.includes('uber') || b.includes('delivery')) {
            return <Utensils className="w-5 h-5 text-rose-400" />;
        }
        return <Gift className="w-5 h-5 text-purple-400" />;
    };

    const getStatusBadge = (status: string) => {
        switch (status) {
            case 'PENDING':
                return (
                    <Badge variant="outline" className="bg-amber-100 hover:bg-amber-100 dark:bg-amber-950/30 text-amber-700 dark:text-amber-400 border-amber-200 dark:border-amber-900 font-semibold flex items-center gap-1">
                        <Clock className="w-3.5 h-3.5" />
                        {t("giftCardStore.statusPending") || "Aguardando aprovação"}
                    </Badge>
                );
            case 'APPROVED':
                return (
                    <Badge variant="outline" className="bg-blue-100 hover:bg-blue-100 dark:bg-blue-950/30 text-blue-700 dark:text-blue-400 border-blue-200 dark:border-blue-900 font-semibold flex items-center gap-1">
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                        {t("giftCardStore.statusApproved") || "Aprovado! Gerando código"}
                    </Badge>
                );
            case 'COMPLETED':
                return (
                    <Badge variant="outline" className="bg-emerald-100 hover:bg-emerald-100 dark:bg-emerald-950/30 text-emerald-700 dark:text-emerald-400 border-emerald-200 dark:border-emerald-900 font-semibold flex items-center gap-1">
                        <CheckCircle2 className="w-3.5 h-3.5" />
                        {t("giftCardStore.statusCompleted") || "Resgatado!"}
                    </Badge>
                );
            case 'FAILED':
                return (
                    <Badge variant="outline" className="bg-rose-100 hover:bg-rose-100 dark:bg-rose-950/30 text-rose-700 dark:text-rose-400 border-rose-200 dark:border-rose-900 font-semibold flex items-center gap-1">
                        <XCircle className="w-3.5 h-3.5" />
                        {t("giftCardStore.statusFailed") || "Recusado"}
                    </Badge>
                );
            default:
                return (
                    <Badge variant="outline" className="font-semibold">
                        {status}
                    </Badge>
                );
        }
    };

    // Filter gift cards into sections
    const gamesCards = giftCards.filter(c => {
        const b = c.brand.toLowerCase();
        return b.includes('roblox') || b.includes('playstation') || b.includes('nintendo') || b.includes('xbox');
    });

    const foodCards = giftCards.filter(c => {
        const b = c.brand.toLowerCase();
        return b.includes('ifood') || b.includes('uber') || b.includes('delivery');
    });

    const otherCards = giftCards.filter(c => {
        const b = c.brand.toLowerCase();
        return !b.includes('roblox') && !b.includes('playstation') && !b.includes('nintendo') && !b.includes('xbox') && !b.includes('ifood') && !b.includes('uber') && !b.includes('delivery');
    });

    if (error) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-blue-50 flex items-center justify-center p-4">
                <Card className="w-full max-w-md text-center shadow-2xl border-purple-200/50 backdrop-blur-md bg-white/95 rounded-3xl p-6">
                    <CardHeader className="pb-4">
                        <div className="w-20 h-20 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4 border-2 border-purple-300">
                            <Gift className="w-10 h-10 text-purple-600 animate-pulse" />
                        </div>
                        <CardTitle className="text-3xl font-extrabold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
                            Acesso Restrito
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        <p className="text-gray-600 text-lg leading-relaxed">
                            {error}
                        </p>
                        <Button
                            onClick={() => window.history.back()}
                            className="w-full bg-gradient-to-r from-purple-600 to-pink-600 text-white font-bold h-12 rounded-2xl hover:from-purple-700 hover:to-pink-700 shadow-lg shadow-purple-500/25 transition-all active:scale-95"
                        >
                            <ArrowLeft className="w-5 h-5 mr-2" />
                            {t("giftCardStore.back") || "Voltar"}
                        </Button>
                    </CardContent>
                </Card>
            </div>
        );
    }

    const currentBalance = child?.balance || 0;

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50/30 to-blue-50 p-4 md:p-8 pb-24 relative overflow-x-hidden">
            {/* Background glowing decorations */}
            <div className="absolute top-20 left-10 w-48 h-48 bg-purple-400/10 rounded-full blur-3xl animate-pulse -z-10" />
            <div className="absolute bottom-20 right-10 w-72 h-72 bg-pink-400/10 rounded-full blur-3xl animate-pulse delay-1000 -z-10" />

            <div className="max-w-5xl mx-auto space-y-8 relative z-10">
                {/* Header Navigation */}
                <div className="flex justify-between items-center">
                    <Button
                        variant="ghost"
                        onClick={() => window.history.back()}
                        className="rounded-full font-bold text-purple-700 hover:text-purple-900 hover:bg-purple-100/50 flex items-center gap-2"
                    >
                        <ArrowLeft className="w-5 h-5" />
                        {t("giftCardStore.back") || "Voltar"}
                    </Button>

                    {/* Fun premium badge */}
                    <div className="px-4 py-1.5 rounded-full bg-purple-600/10 border border-purple-200 text-purple-700 font-extrabold text-sm flex items-center gap-1.5 shadow-sm">
                        <Sparkles className="w-4 h-4 text-purple-500 animate-spin" />
                        Premium Club
                    </div>
                </div>

                {/* Main Hero Card displaying Balance */}
                <Card className="bg-gradient-to-r from-purple-600 via-pink-600 to-pink-500 text-white rounded-3xl border-0 shadow-xl overflow-hidden relative">
                    <div className="absolute top-0 right-0 p-8 opacity-10">
                        <ShoppingCart className="w-40 h-40" />
                    </div>
                    <CardContent className="p-8 flex flex-col md:flex-row justify-between items-center gap-6 relative z-10">
                        <div className="space-y-2 text-center md:text-left">
                            <h1 className="text-3xl md:text-4xl font-black font-heading tracking-tight flex items-center justify-center md:justify-start gap-2">
                                <ShoppingCart className="w-8 h-8" />
                                {t("giftCardStore.title") || "Loja de Recompensas"}
                            </h1>
                            <p className="text-purple-100 text-lg font-medium">
                                {t("giftCardStore.subtitle") || "Troque seu saldo por Gift Cards de verdade!"}
                            </p>
                        </div>
                        {userRole === 'CHILD' && (
                            <div className="bg-white/10 backdrop-blur-md border border-white/20 px-6 py-4 rounded-2xl flex items-center gap-4 shadow-lg shrink-0">
                                <div className="p-3 bg-yellow-400 rounded-2xl shadow-md animate-bounce">
                                    <Coins className="w-8 h-8 text-yellow-950" />
                                </div>
                                <div>
                                    <p className="text-xs uppercase tracking-wider text-purple-100 font-bold">
                                        {t("giftCardStore.balance") || "Seu Saldo Fictício"}
                                    </p>
                                    <p className="text-3xl font-black font-heading text-yellow-300">
                                        R$ {currentBalance.toFixed(2)}
                                    </p>
                                </div>
                            </div>
                        )}
                    </CardContent>
                </Card>

                {loading ? (
                    <div className="flex flex-col items-center justify-center py-20 gap-4">
                        <Loader2 className="w-12 h-12 animate-spin text-purple-600" />
                        <p className="text-purple-700 font-bold text-lg">{t("common.loading") || "Carregando..."}</p>
                    </div>
                ) : (
                    <>
                        {/* Sections & Products */}
                        {giftCards.length === 0 ? (
                            <Card className="border-2 border-dashed border-purple-200/50 bg-white/50 text-center py-16">
                                <CardContent className="flex flex-col items-center gap-4">
                                    <Gift className="w-16 h-16 text-purple-300" />
                                    <p className="text-gray-500 font-bold text-lg">
                                        {t("giftCardStore.noGiftCards") || "Nenhum Gift Card disponível no momento."}
                                    </p>
                                </CardContent>
                            </Card>
                        ) : (
                            <div className="space-y-10">
                                {/* GAMES SECTION */}
                                {gamesCards.length > 0 && (
                                    <div className="space-y-4">
                                        <h2 className="text-2xl font-black text-slate-800 flex items-center gap-2 px-1">
                                            <Gamepad2 className="w-6 h-6 text-indigo-500" />
                                            {t("giftCardStore.gamesCategory") || "Jogos 🎮"}
                                        </h2>
                                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                                            {gamesCards.map((card) => (
                                                <div
                                                    key={card.id}
                                                    onClick={() => userRole === 'CHILD' && setSelectedCard(card)}
                                                    data-testid={`gift-card-${card.id}`}
                                                    className={`group relative rounded-3xl overflow-hidden shadow-lg border border-white/20 transition-all duration-300 ${userRole === 'CHILD' ? 'cursor-pointer hover:-translate-y-2 hover:shadow-2xl' : ''}`}
                                                >
                                                    {/* Card Body styled like physical Gift Card */}
                                                    <div className={`p-6 min-h-[12rem] flex flex-col justify-between ${getCardStyle(card.brand).bg} ${getCardStyle(card.brand).text}`}>
                                                        <div className="flex justify-between items-start">
                                                            <span className={`text-xs uppercase tracking-wider font-extrabold px-2 py-0.5 rounded-full border ${getCardStyle(card.brand).badgeBg}`}>
                                                                {card.brand}
                                                            </span>
                                                            <Coins className="w-6 h-6 text-yellow-300/80 animate-pulse" />
                                                        </div>
                                                        <div className="space-y-1 my-4">
                                                            <h3 className="text-2xl font-black tracking-tight leading-tight">
                                                                {card.name}
                                                            </h3>
                                                            <p className="text-xs text-white/80 line-clamp-2">
                                                                {card.description}
                                                            </p>
                                                        </div>
                                                        <div className="flex justify-between items-end">
                                                            <div>
                                                                <span className="text-[10px] uppercase tracking-widest text-white/60 font-bold block">Valor</span>
                                                                <span className="text-2xl font-black text-yellow-300">R$ {card.value.toFixed(2)}</span>
                                                            </div>
                                                            {userRole === 'CHILD' && (
                                                                <Button size="sm" className="bg-white text-slate-900 font-bold rounded-xl hover:bg-yellow-300 transition-colors">
                                                                    Resgatar
                                                                </Button>
                                                            )}
                                                        </div>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}

                                {/* FOOD SECTION */}
                                {foodCards.length > 0 && (
                                    <div className="space-y-4">
                                        <h2 className="text-2xl font-black text-slate-800 flex items-center gap-2 px-1">
                                            <Utensils className="w-6 h-6 text-rose-500" />
                                            {t("giftCardStore.foodCategory") || "Alimentação & Delivery 🍕"}
                                        </h2>
                                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                                            {foodCards.map((card) => (
                                                <div
                                                    key={card.id}
                                                    onClick={() => userRole === 'CHILD' && setSelectedCard(card)}
                                                    data-testid={`gift-card-${card.id}`}
                                                    className={`group relative rounded-3xl overflow-hidden shadow-lg border border-white/20 transition-all duration-300 ${userRole === 'CHILD' ? 'cursor-pointer hover:-translate-y-2 hover:shadow-2xl' : ''}`}
                                                >
                                                    <div className={`p-6 min-h-[12rem] flex flex-col justify-between ${getCardStyle(card.brand).bg} ${getCardStyle(card.brand).text}`}>
                                                        <div className="flex justify-between items-start">
                                                            <span className={`text-xs uppercase tracking-wider font-extrabold px-2 py-0.5 rounded-full border ${getCardStyle(card.brand).badgeBg}`}>
                                                                {card.brand}
                                                            </span>
                                                            <Coins className="w-6 h-6 text-yellow-300/80 animate-pulse" />
                                                        </div>
                                                        <div className="space-y-1 my-4">
                                                            <h3 className="text-2xl font-black tracking-tight leading-tight">
                                                                {card.name}
                                                            </h3>
                                                            <p className="text-xs text-white/80 line-clamp-2">
                                                                {card.description}
                                                            </p>
                                                        </div>
                                                        <div className="flex justify-between items-end">
                                                            <div>
                                                                <span className="text-[10px] uppercase tracking-widest text-white/60 font-bold block">Valor</span>
                                                                <span className="text-2xl font-black text-yellow-300">R$ {card.value.toFixed(2)}</span>
                                                            </div>
                                                            {userRole === 'CHILD' && (
                                                                <Button size="sm" className="bg-white text-slate-900 font-bold rounded-xl hover:bg-yellow-300 transition-colors">
                                                                    Resgatar
                                                                </Button>
                                                            )}
                                                        </div>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}

                                {/* OTHER SECTION */}
                                {otherCards.length > 0 && (
                                    <div className="space-y-4">
                                        <h2 className="text-2xl font-black text-slate-800 flex items-center gap-2 px-1">
                                            <Gift className="w-6 h-6 text-purple-500" />
                                            {t("giftCardStore.othersCategory") || "Outros 🎁"}
                                        </h2>
                                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                                            {otherCards.map((card) => (
                                                <div
                                                    key={card.id}
                                                    onClick={() => userRole === 'CHILD' && setSelectedCard(card)}
                                                    data-testid={`gift-card-${card.id}`}
                                                    className={`group relative rounded-3xl overflow-hidden shadow-lg border border-white/20 transition-all duration-300 ${userRole === 'CHILD' ? 'cursor-pointer hover:-translate-y-2 hover:shadow-2xl' : ''}`}
                                                >
                                                    <div className={`p-6 min-h-[12rem] flex flex-col justify-between ${getCardStyle(card.brand).bg} ${getCardStyle(card.brand).text}`}>
                                                        <div className="flex justify-between items-start">
                                                            <span className={`text-xs uppercase tracking-wider font-extrabold px-2 py-0.5 rounded-full border ${getCardStyle(card.brand).badgeBg}`}>
                                                                {card.brand}
                                                            </span>
                                                            <Coins className="w-6 h-6 text-yellow-300/80 animate-pulse" />
                                                        </div>
                                                        <div className="space-y-1 my-4">
                                                            <h3 className="text-2xl font-black tracking-tight leading-tight">
                                                                {card.name}
                                                            </h3>
                                                            <p className="text-xs text-white/80 line-clamp-2">
                                                                {card.description}
                                                            </p>
                                                        </div>
                                                        <div className="flex justify-between items-end">
                                                            <div>
                                                                <span className="text-[10px] uppercase tracking-widest text-white/60 font-bold block">Valor</span>
                                                                <span className="text-2xl font-black text-yellow-300">R$ {card.value.toFixed(2)}</span>
                                                            </div>
                                                            {userRole === 'CHILD' && (
                                                                <Button size="sm" className="bg-white text-slate-900 font-bold rounded-xl hover:bg-yellow-300 transition-colors">
                                                                    Resgatar
                                                                </Button>
                                                            )}
                                                        </div>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}

                        {/* TRANSACTION HISTORY SECTION */}
                        <div className="space-y-4 pt-6">
                            <h2 className="text-2xl font-black text-slate-800 flex items-center gap-2 px-1">
                                <ShoppingCart className="w-6 h-6 text-purple-600" />
                                {t("giftCardStore.requestsSection") || "Meus Pedidos 🛍️"}
                            </h2>
                            {transactions.length === 0 ? (
                                <Card className="border border-slate-200/50 bg-white/80 backdrop-blur-md rounded-3xl p-8 text-center">
                                    <CardContent className="flex flex-col items-center gap-3">
                                        <Coins className="w-12 h-12 text-slate-300 animate-pulse" />
                                        <p className="text-slate-400 font-medium">
                                            {t("giftCardStore.noRequests") || "Você ainda não pediu nenhum Gift Card. Complete tarefas para ganhar saldo!"}
                                        </p>
                                    </CardContent>
                                </Card>
                            ) : (
                                <div className="grid gap-4">
                                    {transactions.map((tx) => {
                                        // Find corresponding card details if exists in catalog, otherwise deduce from productId
                                        const matchingCard = giftCards.find(c => c.id === tx.productId);
                                        const brand = matchingCard?.brand || (tx.productId === "1" ? "Roblox" : tx.productId === "2" ? "iFood" : tx.productId === "3" ? "PlayStation" : "Gift Card");
                                        const name = matchingCard?.name || `${brand} R$ ${tx.amount.toFixed(2)}`;
                                        
                                        return (
                                            <Card key={tx.id} data-testid={`transaction-card-${tx.id}`} className="overflow-hidden border border-slate-100 shadow-sm hover:shadow-md transition-shadow rounded-2xl bg-white/90">
                                                <CardContent className="p-5 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                                                    <div className="flex items-start gap-4">
                                                        <div className="p-3 bg-purple-100 text-purple-600 rounded-2xl">
                                                            {getCategoryIcon(brand)}
                                                        </div>
                                                        <div className="space-y-1">
                                                            <h3 className="font-extrabold text-lg text-slate-800 leading-tight">
                                                                {name}
                                                            </h3>
                                                            <p className="text-xs text-slate-400 font-medium">
                                                                Pedido feito em {new Date(tx.createdAt).toLocaleDateString('pt-BR')} às {new Date(tx.createdAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                                                            </p>
                                                        </div>
                                                    </div>

                                                    <div className="flex flex-col md:items-end gap-2 w-full md:w-auto shrink-0">
                                                        <div className="flex items-center justify-between md:justify-end gap-3 w-full md:w-auto">
                                                            <span className="font-black text-xl text-slate-700">R$ {tx.amount.toFixed(2)}</span>
                                                            {getStatusBadge(tx.status)}
                                                        </div>
                                                        
                                                        {tx.status === 'COMPLETED' && tx.pinCode && (
                                                            <div className="mt-2 w-full md:w-auto flex items-center gap-2 bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-200 dark:border-emerald-900 px-3 py-1.5 rounded-xl">
                                                                <span className="font-mono font-bold text-emerald-800 dark:text-emerald-300 select-all tracking-wider text-sm">
                                                                    {tx.pinCode}
                                                                </span>
                                                                <Button
                                                                    size="icon"
                                                                    variant="ghost"
                                                                    className="h-8 w-8 rounded-lg text-emerald-700 hover:text-emerald-900 hover:bg-emerald-100"
                                                                    onClick={() => copyToClipboard(tx.pinCode!, tx.id)}
                                                                >
                                                                    {copiedPinId === tx.id ? (
                                                                        <Check className="w-4 h-4 text-emerald-600" />
                                                                    ) : (
                                                                        <Copy className="w-4 h-4" />
                                                                    )}
                                                                </Button>
                                                            </div>
                                                        )}
                                                    </div>
                                                </CardContent>
                                            </Card>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                    </>
                )}
            </div>

            {/* CONFIRM REQUEST DIALOG (MODAL) */}
            <Dialog open={selectedCard !== null} onOpenChange={(open) => !open && setSelectedCard(null)}>
                {selectedCard && (
                    <DialogContent className="sm:max-w-md bg-white border border-slate-100 rounded-3xl p-6">
                        <DialogHeader>
                            <DialogTitle className="text-2xl font-black text-center bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
                                {t("giftCardStore.requestDialogTitle") || "Confirmar Pedido"}
                            </DialogTitle>
                            <DialogDescription className="text-center font-medium text-slate-500">
                                {t("giftCardStore.requestDialogDesc") || "Você está prestes a pedir este Gift Card para seu responsável."}
                            </DialogDescription>
                        </DialogHeader>

                        <div className="py-6 flex flex-col items-center gap-6">
                            {/* Card preview */}
                            <div className={`w-full max-w-[280px] p-5 min-h-[9rem] rounded-2xl flex flex-col justify-between shadow-lg ${getCardStyle(selectedCard.brand).bg} ${getCardStyle(selectedCard.brand).text}`}>
                                <div className="flex justify-between items-start">
                                    <span className="text-[10px] uppercase tracking-wider font-extrabold px-1.5 py-0.5 rounded-full border border-white/20 bg-white/10">
                                        {selectedCard.brand}
                                    </span>
                                    <Gift className="w-5 h-5 text-white/80" />
                                </div>
                                <h3 className="text-xl font-extrabold leading-snug">{selectedCard.name}</h3>
                                <div className="flex justify-between items-end">
                                    <span className="text-[10px] text-white/60 font-semibold uppercase">Valor</span>
                                    <span className="text-xl font-black text-yellow-300">R$ {selectedCard.value.toFixed(2)}</span>
                                </div>
                            </div>

                            {/* Financial Details */}
                            <div className="w-full bg-slate-50/50 border border-slate-100 rounded-2xl p-4 space-y-3">
                                <div className="flex justify-between text-sm font-medium">
                                    <span className="text-slate-500">{t("giftCardStore.currentBalance") || "Seu saldo atual:"}</span>
                                    <span className="text-slate-700 font-bold">R$ {currentBalance.toFixed(2)}</span>
                                </div>
                                <div className="flex justify-between text-sm font-medium">
                                    <span className="text-slate-500">{t("giftCardStore.price") || "Valor do prêmio:"}</span>
                                    <span className="text-slate-700 font-bold">R$ {selectedCard.value.toFixed(2)}</span>
                                </div>
                                <hr className="border-slate-100" />
                                <div className="flex justify-between text-sm font-medium">
                                    <span className="text-slate-500">{t("giftCardStore.remainingBalance") || "Saldo restante:"}</span>
                                    <span className={`font-black ${currentBalance >= selectedCard.value ? 'text-emerald-600' : 'text-rose-500'}`}>
                                        R$ {(currentBalance - selectedCard.value).toFixed(2)}
                                    </span>
                                </div>
                            </div>

                            {/* Alert / Warning */}
                            {currentBalance >= selectedCard.value ? (
                                <div className="flex items-start gap-3 bg-amber-50 border border-amber-200 p-4 rounded-2xl text-amber-800 text-xs">
                                    <AlertTriangle className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
                                    <p className="leading-relaxed font-medium">
                                        {t("giftCardStore.warningDescription") || "Atenção: Seu responsável receberá um pedido no aplicativo dele e deverá aprovar e pagar o valor real. O saldo do aplicativo é apenas educativo."}
                                    </p>
                                </div>
                            ) : (
                                <div className="flex items-start gap-3 bg-rose-50 border border-rose-200 p-4 rounded-2xl text-rose-800 text-xs w-full">
                                    <AlertTriangle className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
                                    <div className="space-y-1">
                                        <p className="font-bold text-rose-900">{t("giftCardStore.insufficientFunds") || "Saldo Insuficiente!"}</p>
                                        <p className="leading-relaxed font-medium">
                                            {t("giftCardStore.insufficientDesc", { amount: (selectedCard.value - currentBalance).toFixed(2) }) || `Você precisa de mais R$ ${(selectedCard.value - currentBalance).toFixed(2)} para pedir este prêmio. Faça mais tarefas!`}
                                        </p>
                                    </div>
                                </div>
                            )}
                        </div>

                        <DialogFooter className="flex-col sm:flex-row gap-2">
                            <Button
                                variant="outline"
                                className="w-full sm:w-1/2 rounded-xl font-bold"
                                onClick={() => setSelectedCard(null)}
                            >
                                Cancelar
                            </Button>
                            <Button
                                className="w-full sm:w-1/2 rounded-xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 shadow-md"
                                disabled={currentBalance < selectedCard.value || isRequesting}
                                onClick={handleRequestGiftCard}
                            >
                                {isRequesting ? (
                                    <>
                                        <Loader2 className="w-4 h-4 animate-spin mr-2" />
                                        Enviando...
                                    </>
                                ) : (
                                    <>
                                        <Sparkles className="w-4 h-4 mr-2" />
                                        {t("giftCardStore.requestBtn") || "Pedir ao Responsável"}
                                    </>
                                )}
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                )}
            </Dialog>
        </div>
    );
};
