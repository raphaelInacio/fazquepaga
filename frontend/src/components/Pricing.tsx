import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Check } from "lucide-react";
import { useNavigate } from "react-router-dom";

const plans = [
    {
        name: "Básico",
        price: "Grátis",
        description: "Para começar a organizar as tarefas",
        features: [
            "Até 2 filhos",
            "Tarefas ilimitadas",
            "Validação básica de tarefas",
            "Painel dos pais"
        ],
        buttonText: "Começar Grátis",
        popular: false
    },
    {
        name: "Pro",
        price: "R$ 29,90",
        period: "/mês",
        description: "Para famílias que querem automação total",
        features: [
            "Filhos ilimitados",
            "Validação com IA avançada",
            "Integração total com WhatsApp",
            "Relatórios de desempenho",
            "Sugestões de tarefas por IA"
        ],
        buttonText: "Assinar Pro",
        popular: true
    }
];

export const Pricing = () => {
    const navigate = useNavigate();

    return (
        <section id="precos" className="py-24 bg-background">
            <div className="container mx-auto px-4">
                <div className="text-center max-w-3xl mx-auto mb-16">
                    <h2 className="text-4xl font-bold text-foreground mb-4">
                        Planos simples e transparentes
                    </h2>
                    <p className="text-xl text-muted-foreground">
                        Escolha o melhor plano para a educação financeira da sua família
                    </p>
                </div>

                <div className="grid md:grid-cols-2 gap-8 max-w-4xl mx-auto">
                    {plans.map((plan, index) => (
                        <Card
                            key={index}
                            className={`p-8 relative overflow-hidden transition-smooth hover:shadow-glow ${plan.popular
                                    ? "border-primary shadow-soft scale-105 z-10"
                                    : "border-border hover:scale-105"
                                }`}
                        >
                            {plan.popular && (
                                <div className="absolute top-0 right-0 bg-primary text-primary-foreground text-xs font-bold px-3 py-1 rounded-bl-lg">
                                    MAIS POPULAR
                                </div>
                            )}

                            <div className="mb-8">
                                <h3 className="text-2xl font-bold text-foreground mb-2">{plan.name}</h3>
                                <div className="flex items-baseline gap-1">
                                    <span className="text-4xl font-bold text-foreground">{plan.price}</span>
                                    {plan.period && (
                                        <span className="text-muted-foreground">{plan.period}</span>
                                    )}
                                </div>
                                <p className="text-muted-foreground mt-2">{plan.description}</p>
                            </div>

                            <ul className="space-y-4 mb-8">
                                {plan.features.map((feature, i) => (
                                    <li key={i} className="flex items-center gap-3">
                                        <div className="w-6 h-6 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
                                            <Check className="w-4 h-4 text-primary" />
                                        </div>
                                        <span className="text-muted-foreground">{feature}</span>
                                    </li>
                                ))}
                            </ul>

                            <Button
                                className={`w-full ${plan.popular ? "gradient-primary" : "bg-secondary text-secondary-foreground hover:bg-secondary/90"
                                    }`}
                                size="lg"
                                onClick={() => navigate("/register")}
                            >
                                {plan.buttonText}
                            </Button>
                        </Card>
                    ))}
                </div>
            </div>
        </section>
    );
};
