import { Button } from "@/components/ui/button";
import { ArrowRight } from "lucide-react";
import { Card } from "@/components/ui/card";
import { useNavigate } from "react-router-dom";

export const CTA = () => {
  const navigate = useNavigate();

  return (
    <section className="py-24 bg-background">
      <div className="container mx-auto px-4">
        <div className="max-w-4xl mx-auto">
          <Card className="gradient-primary p-12 text-center shadow-glow border-0 overflow-hidden relative">
            <div className="absolute inset-0 bg-gradient-to-br from-primary-glow/20 to-transparent"></div>

            <div className="relative z-10 space-y-6">
              <h2 className="text-4xl font-bold text-primary-foreground">
                Pronto para transformar a educação financeira da sua família?
              </h2>

              <p className="text-xl text-primary-foreground/90 max-w-2xl mx-auto">
                Junte-se a centenas de famílias que já estão ensinando responsabilidade
                e educação financeira de forma moderna e divertida.
              </p>

              <div className="flex flex-col sm:flex-row gap-4 justify-center pt-4">
                <Button
                  size="lg"
                  className="bg-background text-foreground hover:bg-background/90 shadow-soft group"
                  onClick={() => navigate("/register")}
                >
                  Começar agora
                  <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-smooth" />
                </Button>
                <Button
                  variant="outline"
                  size="lg"
                  className="border-primary-foreground/30 text-primary-foreground hover:bg-primary-foreground/10"
                >
                  Agendar demonstração
                </Button>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </section>
  );
};


