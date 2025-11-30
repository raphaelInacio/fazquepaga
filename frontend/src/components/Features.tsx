import { Card } from "@/components/ui/card";
import { Sparkles, Smartphone, Calculator, Shield } from "lucide-react";
import whatsappImage from "@/assets/whatsapp-feature.jpg";
import aiImage from "@/assets/ai-feature.jpg";

const features = [
  {
    icon: Sparkles,
    title: "IA Inteligente",
    description: "Validação automática de tarefas com visão computacional e sugestões personalizadas para cada idade.",
    image: aiImage,
  },
  {
    icon: Smartphone,
    title: "Portal da Criança",
    description: "Uma área exclusiva para seus filhos verem tarefas, acompanharem o saldo e se divertirem com o Modo Aventura.",
    image: whatsappImage,
  },
  {
    icon: Shield,
    title: "Aprovação dos Pais",
    description: "Você no controle. Revise e aprove tarefas concluídas antes de liberar a mesada.",
    image: null,
  },
  {
    icon: Calculator,
    title: "Mesada Automática",
    description: "Sistema inteligente que calcula o valor de cada tarefa baseado em importância e frequência.",
    image: null,
  },
];

export const Features = () => {
  return (
    <section id="recursos" className="py-24 bg-background">
      <div className="container mx-auto px-4">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-4xl font-bold text-foreground mb-4">
            Tudo que você precisa para educar financeiramente
          </h2>
          <p className="text-xl text-muted-foreground">
            Recursos modernos que facilitam o ensino de responsabilidade
          </p>
        </div>

        <div className="grid md:grid-cols-2 gap-8">
          {features.map((feature, index) => (
            <Card
              key={index}
              className="p-8 hover:shadow-glow transition-smooth border-border bg-card overflow-hidden group"
            >
              <div className="flex items-start gap-6">
                <div className="gradient-primary p-4 rounded-xl shadow-soft flex-shrink-0 group-hover:scale-110 transition-smooth">
                  <feature.icon className="w-6 h-6 text-primary-foreground" />
                </div>
                <div className="flex-1">
                  <h3 className="text-xl font-bold text-card-foreground mb-2">
                    {feature.title}
                  </h3>
                  <p className="text-muted-foreground leading-relaxed">
                    {feature.description}
                  </p>
                </div>
              </div>
              {feature.image && (
                <div className="mt-6 rounded-lg overflow-hidden">
                  <img
                    src={feature.image}
                    alt={feature.title}
                    className="w-full h-48 object-cover group-hover:scale-105 transition-smooth"
                  />
                </div>
              )}
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
};
