import { Button } from "@/components/ui/button";
import { Menu, X } from "lucide-react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

export const Header = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const navigate = useNavigate();

  return (
    <header className="sticky top-0 z-50 bg-background/80 backdrop-blur-lg border-b border-border shadow-soft">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-2 cursor-pointer" onClick={() => navigate("/")}>
            <div className="w-10 h-10 gradient-primary rounded-lg flex items-center justify-center shadow-soft">
              <span className="text-xl font-bold text-primary-foreground">F</span>
            </div>
            <span className="text-xl font-bold text-foreground">FazQuePaga</span>
          </div>

          <nav className="hidden md:flex items-center gap-8">
            <a href="#recursos" className="text-muted-foreground hover:text-foreground transition-smooth">
              Recursos
            </a>
            <a href="#como-funciona" className="text-muted-foreground hover:text-foreground transition-smooth">
              Como funciona
            </a>
            <a href="#precos" className="text-muted-foreground hover:text-foreground transition-smooth">
              Preços
            </a>
          </nav>

          <div className="hidden md:flex items-center gap-4">
            <Button variant="ghost" onClick={() => navigate("/dashboard")}>
              Entrar
            </Button>
            <Button variant="default" onClick={() => navigate("/register")}>
              Cadastrar
            </Button>
          </div>

          <button
            className="md:hidden"
            onClick={() => setIsMenuOpen(!isMenuOpen)}
          >
            {isMenuOpen ? <X /> : <Menu />}
          </button>
        </div>

        {isMenuOpen && (
          <div className="md:hidden py-4 border-t border-border animate-fade-in">
            <nav className="flex flex-col gap-4">
              <a href="#recursos" className="text-muted-foreground hover:text-foreground transition-smooth">
                Recursos
              </a>
              <a href="#como-funciona" className="text-muted-foreground hover:text-foreground transition-smooth">
                Como funciona
              </a>
              <a href="#precos" className="text-muted-foreground hover:text-foreground transition-smooth">
                Preços
              </a>
              <div className="flex flex-col gap-2 pt-2">
                <Button variant="ghost" className="w-full" onClick={() => navigate("/dashboard")}>
                  Entrar
                </Button>
                <Button variant="default" className="w-full" onClick={() => navigate("/register")}>
                  Cadastrar
                </Button>
              </div>
            </nav>
          </div>
        )}
      </div>
    </header>
  );
};
