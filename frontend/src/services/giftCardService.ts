import api from '@/lib/api';
import { GiftCard, GiftCardTransaction } from '../types';

export const giftCardService = {
    /**
     * Get available gift cards (Premium only)
     */
    async getAvailableGiftCards(userId: string): Promise<GiftCard[]> {
        const response = await api.get('/api/v1/giftcards', {
            headers: {
                'X-User-Id': userId,
            },
        });
        return response.data;
    },

    /**
     * Request a gift card (creates a transaction in PENDING status)
     */
    async requestGiftCard(productId: string, amount: number, userId: string): Promise<GiftCardTransaction> {
        const response = await api.post(
            '/api/v1/giftcards/requests',
            { productId, amount },
            {
                headers: {
                    'X-User-Id': userId,
                },
            }
        );
        return response.data;
    },

    /**
     * Get all gift card requests for the user (child or parent)
     */
    async getGiftCardRequests(userId: string): Promise<GiftCardTransaction[]> {
        const response = await api.get('/api/v1/giftcards/requests', {
            headers: {
                'X-User-Id': userId,
            },
        });
        return response.data;
    },

    /**
     * Approve a gift card transaction (Parent only)
     */
    async approveGiftCard(transactionId: string, userId: string): Promise<GiftCardTransaction> {
        const response = await api.post(
            `/api/v1/giftcards/requests/${transactionId}/approve`,
            {},
            {
                headers: {
                    'X-User-Id': userId,
                },
            }
        );
        return response.data;
    },
};

