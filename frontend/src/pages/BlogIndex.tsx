import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { SEOMeta } from "@/components/SEOMeta";
import { ArrowRight, BookOpen } from "lucide-react";

// List of static articles generated
const articles = [
  {
    title: "Como ensinar educação financeira para crianças na era digital",
    slug: "como-ensinar-educacao-financeira-para-criancas",
    date: "14 Jun 2026",
    excerpt: "Descubra os desafios e as melhores estratégias para introduzir conceitos financeiros para crianças que já nasceram conectadas.",
    imageUrl: "/og-image.png"
  },
  {
    title: "Qual a idade ideal para começar a dar mesada?",
    slug: "qual-a-idade-ideal-para-comecar-a-dar-mesada",
    date: "14 Jun 2026",
    excerpt: "Existe um momento certo para começar? Veja o que os especialistas dizem sobre a introdução da mesada infantil.",
    imageUrl: "/og-image.png"
  },
  {
    title: "Mesada vs. Pagamento por Tarefas: Qual o melhor método?",
    slug: "mesada-vs-pagamento-por-tarefas",
    date: "14 Jun 2026",
    excerpt: "Entenda as diferenças, prós e contras de dar uma mesada fixa versus pagar apenas por tarefas concluídas.",
    imageUrl: "/og-image.png"
  },
  {
    title: "Tabela de tarefas domésticas por idade (Guia Completo)",
    slug: "tabela-de-tarefas-domesticas-por-idade",
    date: "14 Jun 2026",
    excerpt: "Saiba o que esperar do seu filho em cada fase do desenvolvimento e quais tarefas são adequadas para cada idade.",
    imageUrl: "/og-image.png"
  },
  {
    title: "Como o aplicativo TaskAndPay ensina responsabilidade financeira",
    slug: "como-o-aplicativo-taskandpay-ensina-responsabilidade-financeira",
    date: "14 Jun 2026",
    excerpt: "Conheça as ferramentas e a tecnologia por trás do TaskAndPay que ajudam pais e filhos na jornada da educação financeira.",
    imageUrl: "/og-image.png"
  }
];

export default function BlogIndex() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <SEOMeta 
        title="Blog de Educação Financeira Infantil" 
        description="Dicas, guias e estratégias sobre como gerenciar mesada, tarefas domésticas e ensinar o valor do dinheiro para seus filhos." 
      />
      
      <Header />
      
      <main className="flex-1 py-16">
        <div className="container mx-auto px-4">
          <div className="max-w-3xl mx-auto text-center mb-16 animate-fade-in">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary/10 text-primary mb-6">
              <BookOpen className="w-8 h-8" />
            </div>
            <h1 className="text-4xl md:text-5xl font-bold text-gray-900 mb-6">
              Blog TaskAndPay
            </h1>
            <p className="text-xl text-gray-600">
              O seu guia completo sobre educação financeira infantil, gestão de tarefas e como preparar seus filhos para o futuro.
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8 max-w-7xl mx-auto">
            {articles.map((article) => (
              <a 
                key={article.slug} 
                href={`/blog/${article.slug}.html`}
                className="group bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1 flex flex-col"
              >
                <div className="h-48 overflow-hidden bg-gray-100">
                  <img 
                    src={article.imageUrl} 
                    alt={article.title}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                  />
                </div>
                <div className="p-6 flex flex-col flex-1">
                  <div className="text-sm text-gray-500 mb-3">{article.date}</div>
                  <h2 className="text-xl font-bold text-gray-900 mb-3 group-hover:text-primary transition-colors line-clamp-2">
                    {article.title}
                  </h2>
                  <p className="text-gray-600 line-clamp-3 mb-6 flex-1">
                    {article.excerpt}
                  </p>
                  <div className="inline-flex items-center text-primary font-semibold group-hover:gap-2 transition-all">
                    Ler artigo
                    <ArrowRight className="w-4 h-4 ml-1 opacity-0 -ml-4 group-hover:opacity-100 group-hover:ml-1 transition-all" />
                  </div>
                </div>
              </a>
            ))}
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
