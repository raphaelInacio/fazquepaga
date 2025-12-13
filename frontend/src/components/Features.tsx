import { Card } from "@/components/ui/card";
import { Sparkles, Users, DollarSign, Shield, Edit, Trash2, Globe } from "lucide-react";

const features = [
  {
    icon: Sparkles,
    title: "IA Inteligente",
    description: "Sugestões de tarefas personalizadas por idade e idioma. A IA se adapta ao português ou inglês automaticamente.",
    image: "/tasks-screenshot.png",
  },
  {
    icon: DollarSign,
    title: "Mesada Prevista",
    description: "Sistema inteligente que calcula automaticamente o valor de cada tarefa. Veja quanto seu filho vai ganhar baseado nas tarefas aprovadas.",
    image: "/financial-screenshot.png",
  },
  {
    icon: Users,
    title: "Gestão Completa de Filhos",
    description: "Adicione, edite ou remova filhos facilmente. Cada criança tem seu próprio perfil com idade, telefone e mesada personalizada.",
    image: "/dashboard-screenshot.png",
  },
  {
    icon: Shield,
    title: "Segurança e Controle",
    description: "Apenas você pode gerenciar seus filhos. Sistema com validação de propriedade garante que ninguém mais acesse ou modifique os dados da sua família.",
    image: null,
  },
  {
    icon: Edit,
    title: "Edição Flexível",
    description: "Atualize informações dos seus filhos a qualquer momento. Edição parcial permite mudar apenas o que você precisa.",
    image: null,
  },
  {
    icon: Globe,
    title: "Bilíngue (PT/EN)",
    description: "Interface completa em português e inglês. Troque o idioma e veja até as sugestões de IA mudarem automaticamente.",
    image: null,
  },
];

export const Features = () => {
  return (
    <section id="recursos" className="py-24 bg-background">
      <div className="container mx-auto px-4">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-4xl font-bold text-foreground mb-4">
            Recursos Completos para Educação Financeira
          </h2>
          <p className="text-xl text-muted-foreground">
            Tudo que você precisa para ensinar responsabilidade financeira de forma moderna e eficaz
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
          {features.map((feature, index) => (
            <Card
              key={index}
              className="p-6 hover:shadow-glow transition-smooth border-border bg-card overflow-hidden group"
            >
              <div className="flex flex-col gap-4">
                <div className="gradient-primary p-3 rounded-xl shadow-soft w-fit group-hover:scale-110 transition-smooth">
                  <feature.icon className="w-6 h-6 text-primary-foreground" />
                </div>
                <div>
                  <h3 className="text-xl font-bold text-card-foreground mb-2">
                    {feature.title}
                  </h3>
                  <p className="text-muted-foreground leading-relaxed text-sm">
                    {feature.description}
                  </p>
                </div>
                {feature.image && (
                  <div className="mt-2 rounded-lg overflow-hidden border border-border">
                    <img
                      src={feature.image}
                      alt={feature.title}
                      className="w-full h-40 object-cover object-top group-hover:scale-105 transition-smooth"
                    />
                  </div>
                )}
              </div>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
};
