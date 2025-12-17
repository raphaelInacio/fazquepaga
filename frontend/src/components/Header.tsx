import { Button } from "@/components/ui/button";
import { Menu, X, User, Baby } from "lucide-react";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

export const Header = () => {
  const { t } = useTranslation();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
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
              {t("header.resources")}
            </a>
            <a href="#como-funciona" className="text-muted-foreground hover:text-foreground transition-smooth">
              {t("header.howItWorks")}
            </a>
            <a href="#precos" className="text-muted-foreground hover:text-foreground transition-smooth">
              {t("header.pricing")}
            </a>
          </nav>

          <div className="hidden md:flex items-center gap-4">
            <Button variant="ghost" onClick={() => setIsLoginModalOpen(true)}>
              {t("header.login")}
            </Button>
            <Button variant="default" onClick={() => navigate("/register")}>
              {t("header.register")}
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
                {t("header.resources")}
              </a>
              <a href="#como-funciona" className="text-muted-foreground hover:text-foreground transition-smooth">
                {t("header.howItWorks")}
              </a>
              <a href="#precos" className="text-muted-foreground hover:text-foreground transition-smooth">
                {t("header.pricing")}
              </a>
              <div className="flex flex-col gap-2 pt-2">
                <Button variant="ghost" className="w-full" onClick={() => setIsLoginModalOpen(true)}>
                  {t("header.login")}
                </Button>
                <Button variant="default" className="w-full" onClick={() => navigate("/register")}>
                  {t("header.register")}
                </Button>
              </div>
            </nav>
          </div>
        )}
      </div>

      <Dialog open={isLoginModalOpen} onOpenChange={setIsLoginModalOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="text-center text-2xl font-bold mb-4">{t("header.loginModal.title")}</DialogTitle>
          </DialogHeader>
          <div className="grid grid-cols-2 gap-4">
            <Button
              variant="outline"
              className="h-32 flex flex-col gap-4 hover:border-primary hover:bg-primary/5 transition-all"
              onClick={() => {
                setIsLoginModalOpen(false);
                navigate("/login");
              }}
            >
              <div className="p-3 rounded-full bg-primary/10 text-primary">
                <User className="w-8 h-8" />
              </div>
              <span className="font-semibold text-lg">{t("header.loginModal.parent")}</span>
            </Button>

            <Button
              variant="outline"
              className="h-32 flex flex-col gap-4 hover:border-purple-500 hover:bg-purple-50 transition-all"
              onClick={() => {
                setIsLoginModalOpen(false);
                navigate("/child-login");
              }}
            >
              <div className="p-3 rounded-full bg-purple-100 text-purple-600">
                <Baby className="w-8 h-8" />
              </div>
              <span className="font-semibold text-lg">{t("header.loginModal.child")}</span>
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </header>
  );
};
