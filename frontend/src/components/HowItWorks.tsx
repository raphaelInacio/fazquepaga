import { Card } from "@/components/ui/card";
import { UserPlus, ListTodo, CheckCircle2, DollarSign, Sparkles, Edit3 } from "lucide-react";

const steps = [
  {
    icon: UserPlus,
    title: "Cadastre-se Gratuitamente",
    description: "Crie sua conta de responsável em segundos. Adicione seus filhos com nome, idade e telefone.",
  },
  {
    icon: Edit3,
    title: "Gerencie Seus Filhos",
    description: "Edite informações, defina mesadas personalizadas e mantenha tudo organizado em um só lugar.",
  },
  {
    icon: Sparkles,
    title: "Use a IA para Sugestões",
    description: "Clique em 'Gerar Tarefas com IA' e receba sugestões personalizadas por idade em português ou inglês.",
  },
  {
    icon: ListTodo,
    title: "Crie Tarefas",
    description: "Adicione tarefas manualmente ou use as sugestões da IA. Defina valores, frequência e importância.",
  },
  {
    icon: CheckCircle2,
    title: "Aprove Tarefas Concluídas",
    description: "Revise as tarefas que seus filhos completaram e aprove para liberar a mesada.",
  },
  {
    icon: DollarSign,
    title: "Acompanhe a Mesada Prevista",
    description: "Veja em tempo real quanto cada filho vai ganhar baseado nas tarefas aprovadas.",
  },
];

export const HowItWorks = () => {
  return (
    <section id="como-funciona" className="py-24 gradient-hero">
      <div className="container mx-auto px-4">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-4xl font-bold text-foreground mb-4">
            Como Funciona
          </h2>
          <p className="text-xl text-muted-foreground">
            Em poucos passos, você estará ensinando responsabilidade financeira para seus filhos
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8 max-w-6xl mx-auto">
          {steps.map((step, index) => (
            <Card
              key={index}
              className="p-6 bg-card border-border hover:shadow-glow transition-smooth group relative overflow-hidden"
            >
              <div className="absolute top-4 right-4 text-6xl font-bold text-primary/5 group-hover:text-primary/10 transition-smooth">
                {index + 1}
              </div>
              <div className="relative">
                <div className="gradient-primary p-3 rounded-xl shadow-soft w-fit mb-4 group-hover:scale-110 transition-smooth">
                  <step.icon className="w-6 h-6 text-primary-foreground" />
                </div>
                <h3 className="text-xl font-bold text-card-foreground mb-2">
                  {step.title}
                </h3>
                <p className="text-muted-foreground leading-relaxed text-sm">
                  {step.description}
                </p>
              </div>
            </Card>
          ))}
        </div>

        <div className="text-center mt-12">
          <p className="text-muted-foreground mb-4">
            Pronto para começar?
          </p>
          <a
            href="/register"
            className="inline-flex items-center gap-2 px-6 py-3 bg-primary text-primary-foreground rounded-lg font-semibold hover:opacity-90 transition-smooth"
          >
            Criar Conta Gratuita
          </a>
        </div>
      </div>
    </section>
  );
};
