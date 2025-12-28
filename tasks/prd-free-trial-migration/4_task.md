---
status: completed
---

# Task 4.0: Frontend - Componentes de UI (Badge e Modal)

## Overview

Esta task cria os componentes visuais `TrialBadge` e `TrialExpiredModal`, integrando-os ao layout da aplicação.

**MUST READ**: Antes de iniciar, revise as regras em:
- `docs/ai_guidance/rules/react.md`
- `docs/ai_guidance/rules/internationalization.md`

## Requirements

- `TrialBadge` deve mostrar dias restantes no header
- `TrialExpiredModal` deve ser bloqueante (fullscreen, sem fechar)
- Modal deve ter CTA para checkout Asaas
- Textos devem estar internacionalizados (pt/en)

## Subtasks

- [ ] 4.1 Criar componente `TrialBadge.tsx`
- [ ] 4.2 Criar componente `TrialExpiredModal.tsx`
- [ ] 4.3 Adicionar `TrialBadge` ao Header da aplicação
- [ ] 4.4 Adicionar `TrialExpiredModal` ao layout principal (renderiza quando `isTrialExpired()`)
- [ ] 4.5 Adicionar traduções para pt.json e en.json
- [ ] 4.6 Testar visualmente os componentes

## Implementation Details

### TrialBadge.tsx

```tsx
import { useSubscription } from '../contexts/SubscriptionContext';
import { useTranslation } from 'react-i18next';

export const TrialBadge: React.FC = () => {
    const { isTrialActive, trialDaysRemaining } = useSubscription();
    const { t } = useTranslation();
    
    if (!isTrialActive()) return null;
    
    return (
        <div className="bg-gradient-to-r from-purple-500 to-indigo-500 text-white px-3 py-1 rounded-full text-sm font-medium">
            🎁 {t('trial.badge', { days: trialDaysRemaining })}
        </div>
    );
};
```

### TrialExpiredModal.tsx

```tsx
import { useSubscription } from '../contexts/SubscriptionContext';
import { subscriptionService } from '../services/subscriptionService';
import { useTranslation } from 'react-i18next';

export const TrialExpiredModal: React.FC = () => {
    const { isTrialExpired, isPremium } = useSubscription();
    const { t } = useTranslation();
    const [loading, setLoading] = useState(false);
    
    if (!isTrialExpired() || isPremium()) return null;
    
    const handleSubscribe = async () => {
        setLoading(true);
        const url = await subscriptionService.subscribe();
        window.location.href = url;
    };
    
    return (
        <div className="fixed inset-0 z-50 bg-black/80 flex items-center justify-center">
            <div className="bg-white rounded-2xl p-8 max-w-md text-center">
                <div className="text-4xl mb-4">⏰</div>
                <h2 className="text-2xl font-bold mb-4">{t('trial.expired.title')}</h2>
                <p className="text-gray-600 mb-6">{t('trial.expired.message')}</p>
                <ul className="text-left mb-6 space-y-2">
                    <li>✓ {t('trial.expired.benefit1')}</li>
                    <li>✓ {t('trial.expired.benefit2')}</li>
                    <li>✓ {t('trial.expired.benefit3')}</li>
                </ul>
                <button
                    onClick={handleSubscribe}
                    disabled={loading}
                    className="w-full bg-indigo-600 text-white py-3 rounded-lg font-semibold hover:bg-indigo-700"
                >
                    {loading ? '...' : t('trial.expired.cta')}
                </button>
            </div>
        </div>
    );
};
```

### Traduções (locales/pt.json)

```json
{
  "trial": {
    "badge": "Trial: {{days}} dias restantes",
    "expired": {
      "title": "Seu período de teste terminou",
      "message": "Continue aproveitando o TaskAndPay com todas as funcionalidades:",
      "benefit1": "Tarefas ilimitadas",
      "benefit2": "Sugestões de IA",
      "benefit3": "Loja de Gift Cards",
      "cta": "Assinar Agora"
    }
  }
}
```

### Relevant Files

- `frontend/src/components/TrialBadge.tsx` (NOVO)
- `frontend/src/components/TrialExpiredModal.tsx` (NOVO)
- `frontend/src/components/Header.tsx` (ou equivalente)
- `frontend/src/App.tsx` (ou layout principal)
- `frontend/src/locales/pt.json`
- `frontend/src/locales/en.json`

## Success Criteria

- [ ] Badge aparece no header durante trial ativo
- [ ] Badge mostra dias restantes corretos
- [ ] Modal aparece quando trial expira
- [ ] Modal não pode ser fechado (sem botão X, sem backdrop click)
- [ ] Botão CTA redireciona para checkout Asaas
- [ ] Textos estão traduzidos em pt e en
- [ ] Componentes são responsivos (mobile-first)
