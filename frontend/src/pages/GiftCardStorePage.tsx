import React, { useEffect, useState } from 'react';
import { useSubscription } from '../contexts/SubscriptionContext';
import { giftCardService } from '../services/giftCardService';
import { GiftCard } from '../types';
import { Gift, Loader2, ShoppingCart } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export const GiftCardStorePage: React.FC = () => {
    const { t } = useTranslation();
    const { user, canAccessGiftCardStore } = useSubscription();
    const [giftCards, setGiftCards] = useState<GiftCard[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [redeeming, setRedeeming] = useState<string | null>(null);

    useEffect(() => {
        loadGiftCards();
    }, []);

    const loadGiftCards = async () => {
        if (!user?.id) {
            setError('User not found');
            setLoading(false);
            return;
        }

        if (!canAccessGiftCardStore()) {
            setError('Gift Card store is only available for Premium users');
            setLoading(false);
            return;
        }

        try {
            setLoading(true);
            const cards = await giftCardService.getAvailableGiftCards(user.id);
            setGiftCards(cards);
            setError(null);
        } catch (err: any) {
            console.error('Failed to load gift cards:', err);
            setError(err.response?.data?.message || 'Failed to load gift cards');
        } finally {
            setLoading(false);
        }
    };

    const handleRedeem = async (giftCardId: string) => {
        if (!user?.id) return;

        try {
            setRedeeming(giftCardId);
            const message = await giftCardService.redeemGiftCard(giftCardId, user.id);
            alert(message);
        } catch (err: any) {
            console.error('Failed to redeem gift card:', err);
            alert(err.response?.data?.message || 'Failed to redeem gift card');
        } finally {
            setRedeeming(null);
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-purple-50 to-pink-50 flex items-center justify-center">
                <Loader2 className="w-8 h-8 animate-spin text-purple-600" />
            </div>
        );
    }

    if (error) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-purple-50 to-pink-50 flex items-center justify-center p-4">
                <div className="bg-white rounded-2xl p-8 max-w-md text-center shadow-xl">
                    <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                        <Gift className="w-8 h-8 text-red-600" />
                    </div>
                    <h2 className="text-2xl font-bold mb-2">Acesso Negado</h2>
                    <p className="text-gray-600 mb-6">{error}</p>
                    <button
                        onClick={() => window.history.back()}
                        className="bg-purple-600 text-white px-6 py-2 rounded-lg hover:bg-purple-700 transition-colors"
                    >
                        Voltar
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-50 to-pink-50 py-12 px-4">
            <div className="max-w-6xl mx-auto">
                {/* Header */}
                <div className="text-center mb-12">
                    <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-r from-purple-600 to-pink-600 rounded-full mb-4">
                        <ShoppingCart className="w-10 h-10 text-white" />
                    </div>
                    <h1 className="text-4xl font-bold mb-2 bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
                        Loja de Recompensas
                    </h1>
                    <p className="text-gray-600 text-lg">
                        Troque sua mesada por Gift Cards incríveis!
                    </p>
                </div>

                {/* Gift Cards Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {giftCards.map((card) => (
                        <div
                            key={card.id}
                            className="bg-white rounded-2xl shadow-lg overflow-hidden hover:shadow-xl transition-shadow"
                        >
                            {/* Card Image Placeholder */}
                            <div className="h-48 bg-gradient-to-br from-purple-400 to-pink-400 flex items-center justify-center">
                                <Gift className="w-16 h-16 text-white" />
                            </div>

                            {/* Card Content */}
                            <div className="p-6">
                                <div className="flex items-start justify-between mb-2">
                                    <h3 className="text-xl font-bold">{card.name}</h3>
                                    <span className="bg-green-100 text-green-700 px-3 py-1 rounded-full text-sm font-semibold">
                                        R$ {card.value.toFixed(2)}
                                    </span>
                                </div>
                                <p className="text-gray-600 text-sm mb-4">{card.description}</p>
                                <button
                                    onClick={() => handleRedeem(card.id)}
                                    disabled={redeeming === card.id}
                                    className="w-full bg-gradient-to-r from-purple-600 to-pink-600 text-white py-3 rounded-lg font-semibold hover:from-purple-700 hover:to-pink-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                                >
                                    {redeeming === card.id ? (
                                        <>
                                            <Loader2 className="w-5 h-5 animate-spin" />
                                            Resgatando...
                                        </>
                                    ) : (
                                        <>
                                            <Gift className="w-5 h-5" />
                                            Resgatar Agora
                                        </>
                                    )}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>

                {giftCards.length === 0 && (
                    <div className="text-center py-12">
                        <Gift className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                        <p className="text-gray-500 text-lg">Nenhum Gift Card disponível no momento.</p>
                    </div>
                )}
            </div>
        </div>
    );
};
