import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { SEOMeta } from "@/components/SEOMeta";
import { ArrowRight, BookOpen } from "lucide-react";

// List of static articles generated
const articles = [
  {
    "title": "Os 7 hábitos financeiros que toda criança deveria aprender antes dos 12 anos",
    "slug": "7-habitos-financeiros-para-criancas-antes-dos-12-anos",
    "date": "28 Jun 2026",
    "excerpt": "Hábitos que formam adultos financeiramente saudáveis começam na infância. Veja quais são essenciais.",
    "imageUrl": "https://images.unsplash.com/photo-1518133835878-5a93cc3f89e5?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Adolescentes e dinheiro: como adaptar a educação financeira",
    "slug": "adolescentes-e-dinheiro-educacao-financeira",
    "date": "28 Jun 2026",
    "excerpt": "A adolescência traz novos desafios de consumo, autonomia e planejamento. Aprenda a manter o diálogo financeiro forte e prático nesta fase de transição.",
    "imageUrl": "https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Ansiedade financeira nas crianças: como identificar e ajudar",
    "slug": "ansiedade-financeira-em-criancas-como-ajudar",
    "date": "28 Jun 2026",
    "excerpt": "Sinais de que seu filho está preocupado com dinheiro e estratégias para criar uma relação saudável com o tema.",
    "imageUrl": "https://images.unsplash.com/photo-1555252333-9f8e92e65df9?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Autonomia infantil: como dar responsabilidade sem sobrecarregar",
    "slug": "autonomia-infantil-responsabilidade-sem-sobrecarregar",
    "date": "28 Jun 2026",
    "excerpt": "O equilíbrio certo entre exigir responsabilidade e respeitar o ritmo e a capacidade de cada criança.",
    "imageUrl": "https://images.unsplash.com/photo-1536640712-4d4c36ff0e4e?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como conversar sobre dinheiro com seu filho sem tabu",
    "slug": "como-conversar-sobre-dinheiro-com-seu-filho",
    "date": "28 Jun 2026",
    "excerpt": "Quebre o silêncio financeiro em família e descubra como abordar o tema de forma natural e produtiva.",
    "imageUrl": "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como criar uma cabide de tarefas e recompensas em casa",
    "slug": "como-criar-cabide-de-tarefas-e-recompensas",
    "date": "28 Jun 2026",
    "excerpt": "DIY educativo: monte um sistema visual de tarefas em casa que engaja toda a família e ensina responsabilidade.",
    "imageUrl": "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como criar um contrato familiar de uso de tecnologia",
    "slug": "como-criar-contrato-familiar-uso-de-tecnologia",
    "date": "28 Jun 2026",
    "excerpt": "Regras claras e acordadas em família sobre telas, redes sociais e internet geram mais harmonia e menos conflito.",
    "imageUrl": "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como criar rotinas que funcionam para toda a família",
    "slug": "como-criar-rotinas-que-funcionam-para-toda-a-familia",
    "date": "28 Jun 2026",
    "excerpt": "Guia prático para estabelecer rotinas domésticas que todos os membros da família respeitem e sigam.",
    "imageUrl": "https://images.unsplash.com/photo-1543947931-d9a6e74c38f2?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como criar um sistema de recompensas eficaz para crianças",
    "slug": "como-criar-sistema-de-recompensas-para-criancas",
    "date": "28 Jun 2026",
    "excerpt": "Aprenda a montar um sistema de recompensas que motiva sem criar dependência e ensina valores reais.",
    "imageUrl": "https://images.unsplash.com/photo-1543269865-cbf427effbad?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como definir o valor certo da mesada do seu filho",
    "slug": "como-definir-valor-certo-da-mesada",
    "date": "28 Jun 2026",
    "excerpt": "Critérios objetivos para calcular uma mesada justa, equilibrada e educativa para crianças de todas as idades.",
    "imageUrl": "https://images.unsplash.com/photo-1621981386430-60e01f0f50e2?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como ensinar educação financeira para crianças na era digital",
    "slug": "como-ensinar-educacao-financeira-para-criancas",
    "date": "14 Jun 2026",
    "excerpt": "Descubra os desafios e as melhores estratégias para introduzir conceitos financeiros para crianças que já nasceram conectadas.",
    "imageUrl": "https://images.unsplash.com/photo-1579621970563-ebec7560ff3e?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como evitar criar filhos materialistas",
    "slug": "como-evitar-criar-filhos-materialistas",
    "date": "28 Jun 2026",
    "excerpt": "Reflexões e práticas para ensinar que felicidade não vem de bens materiais, sem abrir mão do conforto.",
    "imageUrl": "https://images.unsplash.com/photo-1474552226712-ac0f0961a954?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como explicar crise financeira para crianças sem assustá-las",
    "slug": "como-explicar-crise-financeira-para-criancas",
    "date": "28 Jun 2026",
    "excerpt": "Em momentos difíceis, a comunicação honesta e adaptada à idade protege a saúde emocional dos filhos. Saiba como falar sobre crise financeira em família.",
    "imageUrl": "https://images.unsplash.com/photo-1584036561566-baf8f5f1b144?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como lidar com a comparação entre filhos sobre mesada e presentes",
    "slug": "como-lidar-com-comparacao-entre-filhos-mesada",
    "date": "28 Jun 2026",
    "excerpt": "'Mas o meu amigo ganha mais!' Como responder e transformar essa situação em oportunidade de aprendizado.",
    "imageUrl": "https://images.unsplash.com/photo-1491438590914-bc09fcaaf77a?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como lidar com crianças que não querem fazer tarefas",
    "slug": "como-lidar-com-criancas-que-nao-querem-fazer-tarefas",
    "date": "28 Jun 2026",
    "excerpt": "Estratégias baseadas em psicologia infantil para motivar filhos resistentes a participar das atividades domésticas.",
    "imageUrl": "https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como o aplicativo TaskAndPay ensina responsabilidade financeira",
    "slug": "como-o-aplicativo-taskandpay-ensina-responsabilidade-financeira",
    "date": "14 Jun 2026",
    "excerpt": "Conheça as ferramentas e a tecnologia por trás do TaskAndPay que ajudam pais e filhos na jornada da educação financeira.",
    "imageUrl": "https://images.unsplash.com/photo-1580514801127-b50e4179339e?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como preparar seu filho para a independência financeira",
    "slug": "como-preparar-seu-filho-para-independencia-financeira",
    "date": "28 Jun 2026",
    "excerpt": "Um roteiro por etapas para guiar seu filho da mesada até a autonomia financeira plena na fase adulta.",
    "imageUrl": "https://images.unsplash.com/photo-1434626881859-194d67b2b86f?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como o TaskAndPay usa IA para validar tarefas dos filhos",
    "slug": "como-taskandpay-usa-ia-para-validar-tarefas",
    "date": "28 Jun 2026",
    "excerpt": "Conheça a tecnologia por trás do TaskAndPay e entenda como a inteligência artificial torna tudo mais justo e transparente.",
    "imageUrl": "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Consumismo infantil: como proteger seu filho dessa armadilha",
    "slug": "consumismo-infantil-como-proteger-seu-filho",
    "date": "28 Jun 2026",
    "excerpt": "Estratégias para criar crianças mais conscientes e resistentes à pressão do consumo excessivo.",
    "imageUrl": "https://images.unsplash.com/photo-1605207862234-f0cf674e8cbc?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Conta poupança infantil: vale a pena abrir para o seu filho?",
    "slug": "conta-poupanca-infantil-vale-a-pena",
    "date": "28 Jun 2026",
    "excerpt": "Entenda as opções disponíveis no mercado e como escolher a melhor forma de guardar o dinheiro do seu filho.",
    "imageUrl": "https://images.unsplash.com/photo-1553729459-efe14ef6055d?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Criança financeiramente inteligente: 5 conversas essenciais",
    "slug": "crianca-financeiramente-inteligente-5-conversas-essenciais",
    "date": "28 Jun 2026",
    "excerpt": "Diálogos transformadores que todo pai precisa ter com o filho para construir uma mente financeira saudável.",
    "imageUrl": "https://images.unsplash.com/photo-1491013516836-7db643ee125a?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #10: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-10",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1580514801127-b50e4179339e?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #11: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-11",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1579621970563-ebec7560ff3e?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #12: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-12",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1611505907380-459f42fb79cd?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #13: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-13",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #14: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-14",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1502920514313-52581002a659?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #15: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-15",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1580514801127-b50e4179339e?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #16: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-16",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1579621970563-ebec7560ff3e?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #17: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-17",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1611505907380-459f42fb79cd?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #18: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-18",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #19: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-19",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1502920514313-52581002a659?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #20: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-20",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1580514801127-b50e4179339e?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #6: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-6",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1579621970563-ebec7560ff3e?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #7: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-7",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1611505907380-459f42fb79cd?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #8: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-8",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Dica de Educação Financeira #9: Construindo um futuro melhor",
    "slug": "dica-de-educacao-financeira-9",
    "date": "14 Jun 2026",
    "excerpt": "Dicas práticas de educação financeira e uso da mesada inteligente para pais e filhos.",
    "imageUrl": "https://images.unsplash.com/photo-1502920514313-52581002a659?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Diferença entre necessidade e desejo: como ensinar para as crianças",
    "slug": "diferenca-entre-necessidade-e-desejo-para-criancas",
    "date": "28 Jun 2026",
    "excerpt": "Uma das lições mais importantes da vida financeira: como ajudar seu filho a distinguir o que é essencial do supérfluo.",
    "imageUrl": "https://images.unsplash.com/photo-1472851294608-062f824d29cc?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Educação financeira na escola: o que os pais precisam saber",
    "slug": "educacao-financeira-na-escola-o-que-pais-precisam-saber",
    "date": "28 Jun 2026",
    "excerpt": "O que o currículo escolar ensina (ou não) sobre dinheiro e como complementar esse aprendizado em casa.",
    "imageUrl": "https://images.unsplash.com/photo-1580582932707-520aed937b7b?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Erros comuns dos pais na educação financeira dos filhos",
    "slug": "erros-comuns-pais-educacao-financeira-filhos",
    "date": "28 Jun 2026",
    "excerpt": "Identifique armadilhas que sabotam o aprendizado financeiro e saiba como evitá-las na sua família.",
    "imageUrl": "https://images.unsplash.com/photo-1541844053589-346841d0b34c?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Filho empreendedor: como estimular o espírito empresarial desde cedo",
    "slug": "filho-empreendedor-estimular-espirito-empresarial",
    "date": "28 Jun 2026",
    "excerpt": "Atividades e mentalidades que desenvolvem criatividade, liderança, resiliência e visão de negócios nas crianças. Aprenda como formar um filho empreendedor.",
    "imageUrl": "https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como criar filhos resilientes através das responsabilidades domésticas",
    "slug": "filhos-resilientes-atraves-das-responsabilidades",
    "date": "28 Jun 2026",
    "excerpt": "A relação entre enfrentar desafios cotidianos e desenvolver a resiliência que acompanha uma criança para a vida.",
    "imageUrl": "https://images.unsplash.com/photo-1516627145497-ae6968895b74?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Filhos únicos vs. irmãos: diferenças na educação financeira",
    "slug": "filhos-unicos-vs-irmaos-educacao-financeira",
    "date": "28 Jun 2026",
    "excerpt": "Como a dinâmica familiar influencia o aprendizado financeiro e como adaptar as estratégias para cada realidade.",
    "imageUrl": "https://images.unsplash.com/photo-1473625247510-8ceb1760943f?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "O futuro da educação financeira: tendências para os próximos anos",
    "slug": "futuro-da-educacao-financeira-tendencias",
    "date": "28 Jun 2026",
    "excerpt": "Como a fintech, IA e novas pedagogias estão redesenhando a forma como as crianças aprendem sobre dinheiro. Conheça o futuro da educação financeira.",
    "imageUrl": "https://images.unsplash.com/photo-1531297484001-80022131f5a1?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Gamificação na educação: como transformar tarefas em aventuras",
    "slug": "gamificacao-na-educacao-tarefas-em-aventuras",
    "date": "28 Jun 2026",
    "excerpt": "Descubra como aplicar princípios de jogos no cotidiano para engajar crianças nas responsabilidades do lar.",
    "imageUrl": "https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como usar histórias e contos para ensinar conceitos financeiros",
    "slug": "historias-e-contos-para-ensinar-conceitos-financeiros",
    "date": "28 Jun 2026",
    "excerpt": "Livros, fábulas e metáforas lúdicas que tornam a poupança, investimentos e o consumo consciente acessíveis e memoráveis para as crianças.",
    "imageUrl": "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "O impacto das tarefas domésticas no desenvolvimento infantil",
    "slug": "impacto-das-tarefas-domesticas-no-desenvolvimento-infantil",
    "date": "28 Jun 2026",
    "excerpt": "Pesquisas mostram que crianças que fazem tarefas crescem mais responsáveis, empáticas e bem-sucedidas.",
    "imageUrl": "https://images.unsplash.com/photo-1484863137850-59afcfe05386?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "A importância da doação na formação do caráter dos filhos",
    "slug": "importancia-da-doacao-formacao-carater-filhos",
    "date": "28 Jun 2026",
    "excerpt": "Ensinar generosidade desde cedo forma adultos mais empáticos, felizes e conscientes do seu papel social.",
    "imageUrl": "https://images.unsplash.com/photo-1582213782179-e0d53f98f2ca?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como a inteligência artificial está transformando a educação dos filhos",
    "slug": "inteligencia-artificial-transformando-educacao-filhos",
    "date": "28 Jun 2026",
    "excerpt": "A IA já faz parte da vida das crianças. Saiba como usar essa tecnologia a favor do aprendizado e do desenvolvimento.",
    "imageUrl": "https://images.unsplash.com/photo-1677442135703-1787eea5ce01?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Jogos que ensinam educação financeira: os melhores para cada idade",
    "slug": "jogos-que-ensinam-educacao-financeira-por-idade",
    "date": "28 Jun 2026",
    "excerpt": "Do Banco Imobiliário ao digital: uma seleção de jogos que desenvolvem habilidades financeiras de forma divertida.",
    "imageUrl": "https://images.unsplash.com/photo-1611996575749-79a3a250f948?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Juros compostos explicados para crianças: uma aula de vida",
    "slug": "juros-compostos-explicados-para-criancas",
    "date": "28 Jun 2026",
    "excerpt": "Como explicar o conceito mais poderoso das finanças de forma simples, divertida e que as crianças entendam.",
    "imageUrl": "https://images.unsplash.com/photo-1633158829585-23ba8f7c8caf?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Aplicativos para ensinar finanças para crianças: os melhores de 2026",
    "slug": "melhores-aplicativos-financas-para-criancas-2026",
    "date": "28 Jun 2026",
    "excerpt": "Um guia curado com as melhores ferramentas digitais para tornar a educação financeira interativa e atraente.",
    "imageUrl": "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Mesada digital vs. dinheiro físico: qual é melhor para crianças?",
    "slug": "mesada-digital-vs-dinheiro-fisico-qual-e-melhor",
    "date": "28 Jun 2026",
    "excerpt": "Uma análise honesta dos prós e contras de cada forma de transferir mesada para os filhos na era do Pix.",
    "imageUrl": "https://images.unsplash.com/photo-1601597111158-2fceff292cdc?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como a mesada ajuda no desenvolvimento da autoestima infantil",
    "slug": "mesada-e-autoestima-infantil",
    "date": "28 Jun 2026",
    "excerpt": "Quando a criança gerencia seu próprio dinheiro, ela desenvolve confiança, autonomia e senso de competência.",
    "imageUrl": "https://images.unsplash.com/photo-1588072432836-e10032774350?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Mesada vs. Pagamento por Tarefas: Qual o melhor método?",
    "slug": "mesada-vs-pagamento-por-tarefas",
    "date": "14 Jun 2026",
    "excerpt": "Entenda as diferenças, prós e contras de dar uma mesada fixa versus pagar apenas por tarefas concluídas.",
    "imageUrl": "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como usar o método dos 3 potes para ensinar finanças",
    "slug": "metodo-dos-3-potes-para-ensinar-financas",
    "date": "28 Jun 2026",
    "excerpt": "Um método simples e poderoso: gastar, poupar e doar. Aprenda a aplicar com seus filhos usando o método dos 3 potes.",
    "imageUrl": "https://images.unsplash.com/photo-1563013544-824ae1b704d3?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Natal e Páscoa: como gerenciar os gastos com presentes para filhos",
    "slug": "natal-e-pascoa-como-gerenciar-gastos-com-presentes",
    "date": "28 Jun 2026",
    "excerpt": "Dicas para manter o equilíbrio entre presentear com amor e não criar expectativas financeiras insustentáveis.",
    "imageUrl": "https://images.unsplash.com/photo-1512389142860-9c449e58a543?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Organização familiar: ferramentas digitais que toda família precisa",
    "slug": "organizacao-familiar-ferramentas-digitais",
    "date": "28 Jun 2026",
    "excerpt": "Da lista de tarefas compartilhada ao calendário integrado: como a tecnologia pode unir e organizar sua família.",
    "imageUrl": "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "O papel dos avós na educação financeira dos netos",
    "slug": "papel-dos-avos-na-educacao-financeira-dos-netos",
    "date": "28 Jun 2026",
    "excerpt": "Como envolver os avós de forma positiva e alinhada com os valores financeiros que você quer transmitir.",
    "imageUrl": "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Por que dar mesada é um investimento no futuro do seu filho",
    "slug": "por-que-dar-mesada-e-um-investimento-no-futuro",
    "date": "28 Jun 2026",
    "excerpt": "O valor real por trás da mesada vai muito além do dinheiro: é uma escola de vida que dura para sempre.",
    "imageUrl": "https://images.unsplash.com/photo-1459180177440-0dd76a459e87?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Poupança infantil: como ensinar seu filho a guardar dinheiro",
    "slug": "poupanca-infantil-como-ensinar-seu-filho-a-guardar-dinheiro",
    "date": "28 Jun 2026",
    "excerpt": "Estratégias práticas e divertidas para criar o hábito da poupança desde cedo e preparar o futuro do seu filho.",
    "imageUrl": "https://images.unsplash.com/photo-1611348586804-61bf6c080437?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Privacidade digital das crianças: o que os pais precisam saber",
    "slug": "privacidade-digital-das-criancas",
    "date": "28 Jun 2026",
    "excerpt": "Como proteger os dados e a privacidade dos seus filhos no mundo digital sem sufocar sua autonomia.",
    "imageUrl": "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Psicologia das recompensas: por que algumas funcionam e outras não",
    "slug": "psicologia-das-recompensas-para-criancas",
    "date": "28 Jun 2026",
    "excerpt": "A ciência por trás da motivação infantil e como construir sistemas de incentivo realmente eficazes.",
    "imageUrl": "https://images.unsplash.com/photo-1597743537789-3b42bc1c2f54?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Qual a idade ideal para começar a dar mesada?",
    "slug": "qual-a-idade-ideal-para-comecar-a-dar-mesada",
    "date": "14 Jun 2026",
    "excerpt": "Existe um momento certo para começar? Veja o que os especialistas dizem sobre a introdução da mesada infantil.",
    "imageUrl": "https://images.unsplash.com/photo-1611505907380-459f42fb79cd?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Tabela de tarefas domésticas por idade (Guia Completo)",
    "slug": "tabela-de-tarefas-domesticas-por-idade",
    "date": "14 Jun 2026",
    "excerpt": "Saiba o que esperar do seu filho em cada fase do desenvolvimento e quais tarefas são adequadas para cada idade.",
    "imageUrl": "https://images.unsplash.com/photo-1502920514313-52581002a659?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Tarefas domésticas por idade: o guia definitivo para pais",
    "slug": "tarefas-domesticas-por-idade-guia-definitivo",
    "date": "28 Jun 2026",
    "excerpt": "Saiba exatamente quais tarefas seu filho pode fazer em cada fase da vida e como incentivá-lo.",
    "imageUrl": "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como o TaskAndPay transforma a relação familiar com tarefas e dinheiro",
    "slug": "taskandpay-transforma-relacao-familiar-tarefas-dinheiro",
    "date": "28 Jun 2026",
    "excerpt": "Histórias reais e funcionalidades que mostram como o aplicativo muda a dinâmica de responsabilidade, educação financeira e recompensa nas famílias brasileiras.",
    "imageUrl": "https://images.unsplash.com/photo-1511895426328-dc8714191011?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como a tecnologia pode ajudar na educação financeira dos filhos",
    "slug": "tecnologia-na-educacao-financeira-dos-filhos",
    "date": "28 Jun 2026",
    "excerpt": "Apps, jogos e ferramentas digitais que tornam o aprendizado financeiro mais divertido e eficaz para crianças.",
    "imageUrl": "https://images.unsplash.com/photo-1517180102446-f3ece451e9d8?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Tempo de tela vs. responsabilidades: como encontrar o equilíbrio",
    "slug": "tempo-de-tela-vs-responsabilidades-como-equilibrar",
    "date": "28 Jun 2026",
    "excerpt": "Dicas práticas para que o uso de dispositivos não substitua as obrigações e o desenvolvimento saudável das crianças.",
    "imageUrl": "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Tesouro Direto para crianças: como começar a investir cedo",
    "slug": "tesouro-direto-para-criancas-como-comecar",
    "date": "28 Jun 2026",
    "excerpt": "Guia completo para pais que querem apresentar o mundo dos investimentos para os filhos ainda na infância.",
    "imageUrl": "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&q=80&w=800"
  },
  {
    "title": "Como usar o WhatsApp para se comunicar com seus filhos sobre tarefas",
    "slug": "whatsapp-para-comunicar-tarefas-com-filhos",
    "date": "28 Jun 2026",
    "excerpt": "Estratégias para usar o aplicativo mais popular do Brasil de forma educativa, leve e produtiva para gerenciar tarefas domésticas com as crianças.",
    "imageUrl": "https://images.unsplash.com/photo-1614332287897-cdc485fa562d?auto=format&fit=crop&q=80&w=800"
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
