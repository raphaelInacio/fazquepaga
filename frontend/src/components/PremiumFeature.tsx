import React from 'react';
import { useSubscription } from '../contexts/SubscriptionContext';
import { Lock, Sparkles } from 'lucide-react';

interface PremiumFeatureProps {
    children: React.ReactNode;
    featureName?: string;
}

/**
 * Wrapper component that blocks access to premium features for Free users.
 * Shows a paywall modal when a Free user tries to access the feature.
 */
export const PremiumFeature: React.FC<PremiumFeatureProps> = ({ children, featureName = 'Esta funcionalidade' }) => {
    const { isPremium } = useSubscription();
    const [showPaywall, setShowPaywall] = React.useState(false);

    if (isPremium()) {
        return <>{children}</>;
    }

    return (
        <>
            <div className="relative">
                <div className="blur-sm pointer-events-none opacity-50">
                    {children}
                </div>
                <div className="absolute inset-0 flex items-center justify-center">
                    <button
                        onClick={() => setShowPaywall(true)}
                        className="bg-gradient-to-r from-purple-600 to-pink-600 text-white px-6 py-3 rounded-lg font-semibold flex items-center gap-2 hover:from-purple-700 hover:to-pink-700 transition-all shadow-lg"
                    >
                        <Lock className="w-5 h-5" />
                        Desbloquear Premium
                    </button>
                </div>
            </div>

            {showPaywall && (
                <PaywallModal
                    featureName={featureName}
                    onClose={() => setShowPaywall(false)}
                />
            )}
        </>
    );
};

interface PaywallModalProps {
    featureName: string;
    onClose: () => void;
}

const PaywallModal: React.FC<PaywallModalProps> = ({ featureName, onClose }) => {
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl max-w-md w-full p-8 relative">
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 text-gray-400 hover:text-gray-600"
                >
                    ✕
                </button>

                <div className="text-center">
                    <div className="w-16 h-16 bg-gradient-to-r from-purple-600 to-pink-600 rounded-full flex items-center justify-center mx-auto mb-4">
                        <Sparkles className="w-8 h-8 text-white" />
                    </div>

                    <h2 className="text-2xl font-bold mb-2">Upgrade para Premium</h2>
                    <p className="text-gray-600 mb-6">
                        {featureName} está disponível apenas no plano Premium.
                    </p>

                    <div className="bg-gradient-to-br from-purple-50 to-pink-50 rounded-xl p-6 mb-6 text-left">
                        <h3 className="font-semibold mb-3 text-lg">Com o Premium você tem:</h3>
                        <ul className="space-y-2 text-sm">
                            <li className="flex items-start gap-2">
                                <span className="text-green-500 mt-0.5">✓</span>
                                <span>Tarefas ilimitadas</span>
                            </li>
                            <li className="flex items-start gap-2">
                                <span className="text-green-500 mt-0.5">✓</span>
                                <span>Sugestões de tarefas por IA</span>
                            </li>
                            <li className="flex items-start gap-2">
                                <span className="text-green-500 mt-0.5">✓</span>
                                <span>Validação automática de fotos</span>
                            </li>
                            <li className="flex items-start gap-2">
                                <span className="text-green-500 mt-0.5">✓</span>
                                <span>Relatórios de comportamento</span>
                            </li>
                            <li className="flex items-start gap-2">
                                <span className="text-green-500 mt-0.5">✓</span>
                                <span>Loja de Gift Cards (Roblox, iFood, etc.)</span>
                            </li>
                        </ul>
                    </div>

                    <button
                        onClick={() => {
                            // TODO: Implement upgrade flow
                            alert('Funcionalidade de upgrade em desenvolvimento!');
                        }}
                        className="w-full bg-gradient-to-r from-purple-600 to-pink-600 text-white py-3 rounded-lg font-semibold hover:from-purple-700 hover:to-pink-700 transition-all"
                    >
                        Fazer Upgrade Agora
                    </button>

                    <button
                        onClick={onClose}
                        className="w-full mt-3 text-gray-500 hover:text-gray-700 py-2"
                    >
                        Continuar com Plano Free
                    </button>
                </div>
            </div>
        </div>
    );
};
