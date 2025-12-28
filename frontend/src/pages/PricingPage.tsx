
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Check, Loader2, Star, Gift } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { subscriptionService } from "@/services/subscriptionService";
import { useSubscription } from "@/contexts/SubscriptionContext";
import { toast } from "sonner";
import { navigateTo } from "@/lib/utils";

export default function PricingPage() {
    const { t } = useTranslation();
    const { isPremium, isTrialActive, trialDaysRemaining } = useSubscription();
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);

    const handleSubscribe = async () => {
        setIsLoading(true);
        try {
            const response = await subscriptionService.subscribe();
            if (response.checkoutUrl) {
                navigateTo(response.checkoutUrl);
            } else {
                toast.error("Failed to generate checkout link.");
            }
        } catch (error) {
            console.error(error);
            toast.error("An error occurred while starting the subscription.");
        } finally {
            setIsLoading(false);
        }
    };

    const features = [
        "Tarefas ilimitadas",
        "Filhos ilimitados",
        "Sugestões de tarefas por IA",
        "Validação com IA avançada",
        "Loja de Gift Cards",
        "Integração com WhatsApp",
        "Relatórios financeiros"
    ];

    return (
        <div className="container mx-auto py-10 px-4">
            <Button
                variant="ghost"
                onClick={() => navigate(-1)}
                className="mb-4"
            >
                {t("common.back")}
            </Button>
            <div className="text-center mb-10">
                <h1 className="text-4xl font-bold mb-4">
                    {isTrialActive() ? `Seu Trial: ${trialDaysRemaining} dias restantes` : "Assine o Premium"}
                </h1>
                <p className="text-xl text-muted-foreground">
                    Desbloqueie todo o potencial do TaskAndPay para sua família
                </p>
            </div>

            <div className="max-w-lg mx-auto">
                {/* Premium Plan */}
                <Card className="border-2 border-primary shadow-lg relative overflow-hidden">
                    <div className="absolute top-0 right-0 bg-gradient-to-r from-purple-500 to-indigo-500 text-white px-3 py-1 text-sm font-medium rounded-bl-lg flex items-center gap-1">
                        <Gift className="w-4 h-4" /> Oferta Beta Testers
                    </div>
                    <CardHeader className="pt-8">
                        <CardTitle className="text-2xl flex items-center gap-2">
                            Premium <Star className="h-5 w-5 fill-yellow-400 text-yellow-400" />
                        </CardTitle>
                        <CardDescription>Acesso completo a todas as funcionalidades</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="flex items-baseline gap-2">
                            <span className="text-xl text-muted-foreground line-through">R$ 29,90</span>
                            <span className="text-4xl font-bold text-primary">R$ 9,90</span>
                            <span className="text-sm font-normal text-muted-foreground">/mês</span>
                        </div>
                        {isTrialActive() && (
                            <div className="bg-purple-100 text-purple-700 px-4 py-2 rounded-lg text-center font-medium">
                                🎁 Seu trial está ativo! Experimente tudo grátis.
                            </div>
                        )}
                        <ul className="space-y-2">
                            {features.map((feature, i) => (
                                <li key={i} className="flex items-center gap-2">
                                    <Check className="h-4 w-4 text-primary" /> {feature}
                                </li>
                            ))}
                        </ul>
                    </CardContent>
                    <CardFooter>
                        {isPremium() ? (
                            <Button className="w-full" variant="secondary" disabled>
                                ✓ Plano Ativo
                            </Button>
                        ) : (
                            <Button className="w-full bg-gradient-to-r from-purple-500 to-indigo-500 hover:from-purple-600 hover:to-indigo-600" onClick={handleSubscribe} disabled={isLoading}>
                                {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                                {isTrialActive() ? "Assinar Agora" : "Começar Trial Grátis"}
                            </Button>
                        )}
                    </CardFooter>
                </Card>

                <p className="text-center text-muted-foreground text-sm mt-6">
                    3 dias grátis para experimentar • Cancele a qualquer momento
                </p>
            </div>
        </div>
    );
}
