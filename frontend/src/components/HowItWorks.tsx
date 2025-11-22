import { Card } from "@/components/ui/card";
import { UserPlus, ListChecks, Smartphone, Coins } from "lucide-react";

const steps = [
  {
    icon: UserPlus,
    title: "1. Cadastre sua família",
    description: "Crie uma conta e adicione o perfil do seu filho em minutos.",
  },
  {
    icon: ListChecks,
    title: "2. Defina tarefas e valores",
    description: "Crie tarefas diárias, semanais ou únicas. A IA sugere valores baseados na importância.",
  },
  {
    icon: Smartphone,
    title: "3. Filhos completam via WhatsApp",
    description: "Crianças enviam fotos das tarefas concluídas de forma super simples.",
  },
  {
    icon: Coins,
    title: "4. Aprove e acompanhe",
    description: "Revise as tarefas e veja a mesada sendo calculada automaticamente.",
  },
];

export const HowItWorks = () => {
  return (
    <section id="como-funciona" className="py-24 gradient-hero">
      <div className="container mx-auto px-4">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-4xl font-bold text-foreground mb-4">
            Como funciona
          </h2>
          <p className="text-xl text-muted-foreground">
            Em 4 passos simples, comece a ensinar responsabilidade financeira
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
          {steps.map((step, index) => (
            <Card
              key={index}
              className="p-6 text-center hover:shadow-soft transition-smooth border-border bg-card relative overflow-hidden group"
            >
              <div className="absolute top-0 right-0 w-24 h-24 gradient-primary opacity-5 rounded-full -mr-12 -mt-12 group-hover:scale-150 transition-smooth"></div>

              <div className="gradient-secondary w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-soft group-hover:scale-110 transition-smooth">
                <step.icon className="w-8 h-8 text-secondary-foreground" />
              </div>

              <h3 className="text-lg font-bold text-card-foreground mb-2">
                {step.title}
              </h3>

              <p className="text-muted-foreground text-sm leading-relaxed">
                {step.description}
              </p>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
};
