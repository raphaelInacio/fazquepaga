import { Button } from "@/components/ui/button";
import { ArrowRight, CheckCircle2 } from "lucide-react";
import { Mascot } from "./Mascot";
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
              Ensine seus filhos o valor do trabalho e do dinheiro de forma prática e divertida.
              Gerencie tarefas, mesadas e acompanhe o desenvolvimento financeiro da sua família.
            </p>

            <div className="flex flex-col sm:flex-row gap-4">
              <Button variant="hero" size="lg" className="group" onClick={() => navigate("/register")}>
                Começar gratuitamente
                <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-smooth" />
              </Button>
              <Button variant="outline" size="lg" onClick={() => navigate("/dashboard")}>
                Ver demonstração
              </Button>
            </div>

            <div className="flex items-center gap-8 pt-4">
              <div>
                <p className="text-3xl font-bold text-foreground">100%</p>
                <p className="text-sm text-muted-foreground">Gratuito</p>
              </div>
              <div className="h-12 w-px bg-border"></div>
              <div>
                <p className="text-3xl font-bold text-foreground">IA</p>
                <p className="text-sm text-muted-foreground">Integrada</p>
              </div>
              <div className="h-12 w-px bg-border"></div>
              <div>
                <p className="text-3xl font-bold text-foreground">PT/EN</p>
                <p className="text-sm text-muted-foreground">Bilíngue</p>
              </div>
            </div>


          </div>


          <div className="relative">

            <div className="absolute inset-0 gradient-primary opacity-20 blur-3xl rounded-full"></div>
            <div className="relative z-10 flex justify-center items-center">
              <Mascot
                state="default"
                className="w-full max-w-md animate-in zoom-in duration-1000 hover:scale-105 transition-transform mix-blend-multiply"
                width={800}
              />
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
