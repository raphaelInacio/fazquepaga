import { useState } from 'react';
import { useSubscription } from '../contexts/SubscriptionContext';
import { subscriptionService } from '../services/subscriptionService';
import { useTranslation } from 'react-i18next';
import { Button } from './ui/button';
import { Loader2 } from 'lucide-react';

export const TrialExpiredModal: React.FC = () => {
    const { isTrialExpired, isPremium } = useSubscription();
    const { t } = useTranslation();
    const [loading, setLoading] = useState(false);

    // Don't show if trial is not expired or user is premium
    if (!isTrialExpired() || isPremium()) return null;

    const handleSubscribe = async () => {
        setLoading(true);
        try {
            const response = await subscriptionService.subscribe();
            window.location.href = response.checkoutUrl;
        } catch (error) {
            console.error('Failed to redirect to checkout:', error);
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-[100] bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
            <div className="bg-white dark:bg-gray-900 rounded-2xl p-8 max-w-md w-full text-center shadow-2xl animate-fade-in">
                <div className="text-6xl mb-6">⏰</div>
                <h2 className="text-2xl font-bold mb-4 text-gray-900 dark:text-white">
                    {t('trial.expired.title')}
                </h2>
                <p className="text-gray-600 dark:text-gray-400 mb-6">
                    {t('trial.expired.message')}
                </p>
                <ul className="text-left mb-8 space-y-3">
                    <li className="flex items-center gap-2 text-gray-700 dark:text-gray-300">
                        <span className="text-green-500 font-bold">✓</span>
                        {t('trial.expired.benefit1')}
                    </li>
                    <li className="flex items-center gap-2 text-gray-700 dark:text-gray-300">
                        <span className="text-green-500 font-bold">✓</span>
                        {t('trial.expired.benefit2')}
                    </li>
                    <li className="flex items-center gap-2 text-gray-700 dark:text-gray-300">
                        <span className="text-green-500 font-bold">✓</span>
                        {t('trial.expired.benefit3')}
                    </li>
                    <li className="flex items-center gap-2 text-gray-700 dark:text-gray-300">
                        <span className="text-green-500 font-bold">✓</span>
                        {t('trial.expired.benefit4')}
                    </li>
                </ul>
                <Button
                    onClick={handleSubscribe}
                    disabled={loading}
                    className="w-full bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-white py-4 rounded-xl font-bold text-lg shadow-lg shadow-indigo-500/30 transition-all"
                    data-testid="subscribe-now-button"
                >
                    {loading ? (
                        <Loader2 className="h-5 w-5 animate-spin" />
                    ) : (
                        t('trial.expired.cta')
                    )}
                </Button>
            </div>
        </div>
    );
};
