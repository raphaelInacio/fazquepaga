export const Footer = () => {
  return (
    <footer className="bg-card border-t border-border py-12">
      <div className="container mx-auto px-4">
        <div className="grid md:grid-cols-4 gap-8">
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 gradient-primary rounded-lg flex items-center justify-center">
                <span className="text-lg font-bold text-primary-foreground">F</span>
              </div>
              <span className="text-lg font-bold text-foreground">FazQuePaga</span>
            </div>
            <p className="text-sm text-muted-foreground">
              Transformando tarefas em educação financeira.
            </p>
          </div>

          <div>
            <h4 className="font-semibold text-foreground mb-4">Produto</h4>
            <ul className="space-y-2">
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-smooth">Recursos</a></li>
              <li><a href="/#pricing" className="text-sm text-muted-foreground hover:text-foreground transition-smooth">Preços</a></li>
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-smooth">FAQ</a></li>
            </ul>
          </div>

          <div>
            <h4 className="font-semibold text-foreground mb-4">Empresa</h4>
            <ul className="space-y-2">
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-smooth">Sobre</a></li>
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-smooth">Blog</a></li>
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-smooth">Contato</a></li>
            </ul>
          </div>

          <div>
            <h4 className="font-semibold text-foreground mb-4">Legal</h4>
            <ul className="space-y-2">
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-smooth">Privacidade</a></li>
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-smooth">Termos</a></li>
            </ul>
          </div>
        </div>

        <div className="border-t border-border mt-8 pt-8 text-center">
          <p className="text-sm text-muted-foreground">
            © 2025 FazQuePaga. Todos os direitos reservados.
          </p>
        </div>
      </div>
    </footer>
  );
};
