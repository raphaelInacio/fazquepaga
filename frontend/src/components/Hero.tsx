import { Button } from "@/components/ui/button";
import { ArrowRight, CheckCircle2 } from "lucide-react";
import heroImage from "@/assets/hero-image.jpg";
import { useNavigate } from "react-router-dom";

export const Hero = () => {
  const navigate = useNavigate();

  return (
    <section className="gradient-hero min-h-[90vh] flex items-center">
      <div className="container mx-auto px-4 py-16">
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          <div className="space-y-8 animate-fade-in">
            <div className="inline-flex items-center gap-2 px-4 py-2 bg-card rounded-full shadow-soft">
              <CheckCircle2 className="w-4 h-4 text-primary" />
              <span className="text-sm font-medium text-muted-foreground">
                Plataforma com IA integrada
              </span>
            </div>

            <h1 className="text-5xl lg:text-6xl font-bold text-foreground leading-tight">
              Transforme tarefas em{" "}
              <span className="gradient-primary bg-clip-text text-transparent">
                responsabilidade financeira
              </span>
            </h1>

            <p className="text-xl text-muted-foreground leading-relaxed max-w-xl">
              Ajude seus filhos a aprenderem o valor do trabalho e do dinheiro.
              Uma plataforma moderna que conecta pais e filhos através de tarefas,
              mesadas e educação financeira.
            </p>

            <div className="flex flex-col sm:flex-row gap-4">
              <Button variant="hero" size="lg" className="group" onClick={() => navigate("/register")}>
                Começar gratuitamente
                <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-smooth" />
              </Button>
              <Button variant="outline" size="lg">
                Ver demonstração
              </Button>
            </div>

            <div className="flex items-center gap-8 pt-4">
              <div>
                <p className="text-3xl font-bold text-foreground">500+</p>
                <p className="text-sm text-muted-foreground">Famílias ativas</p>
              </div>
              <div className="h-12 w-px bg-border"></div>
              <div>
                <p className="text-3xl font-bold text-foreground">10k+</p>
                <p className="text-sm text-muted-foreground">Tarefas concluídas</p>
              </div>
            </div>
          </div>

          <div className="relative">
            <div className="absolute inset-0 gradient-primary opacity-20 blur-3xl rounded-full"></div>
            <img
              src={heroImage}
              alt="Família celebrando conquistas com o FazQuePaga"
              className="relative rounded-2xl shadow-soft w-full h-auto"
            />
          </div>
        </div>
      </div>
    </section>
  );
};
