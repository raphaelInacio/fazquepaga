import { useSubscription } from '../contexts/SubscriptionContext';
import { useTranslation } from 'react-i18next';

export const TrialBadge: React.FC = () => {
    const { isTrialActive, trialDaysRemaining } = useSubscription();
    const { t } = useTranslation();

    if (!isTrialActive()) return null;

    return (
        <div className="bg-gradient-to-r from-purple-500 to-indigo-500 text-white px-3 py-1 rounded-full text-sm font-medium shadow-lg shadow-purple-500/20 flex items-center gap-1">
            <span>🎁</span>
            <span>{t('trial.badge', { days: trialDaysRemaining })}</span>
        </div>
    );
};
