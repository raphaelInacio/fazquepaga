import axios from 'axios';
import { GiftCard } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const giftCardService = {
    /**
     * Get available gift cards (Premium only)
     */
    async getAvailableGiftCards(userId: string): Promise<GiftCard[]> {
        const response = await axios.get(`${API_BASE_URL}/api/v1/giftcards`, {
            headers: {
                'X-User-Id': userId,
            },
        });
        return response.data;
    },

    /**
     * Redeem a gift card (Premium only)
     */
    async redeemGiftCard(giftCardId: string, userId: string): Promise<string> {
        const response = await axios.post(
            `${API_BASE_URL}/api/v1/giftcards/${giftCardId}/redeem`,
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
