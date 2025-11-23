# Documento de Requisitos do Produto (PRD): TaskAndPay

## Visão Geral

O TaskAndPay é uma plataforma de Software como Serviço (SaaS) projetada para pais e filhos (menores de 18 anos) para gerenciar tarefas e mesadas de uma forma moderna e envolvente. O sistema permite que os pais atribuam valor às atividades de seus filhos, acompanhem sua conclusão e automatizem o cálculo da mesada. Ele integra IA para sugestões de tarefas e oferece uma interface web simples e dedicada para as crianças interagirem com suas responsabilidades, usando o WhatsApp como um canal de notificação opcional.

O problema central que ele resolve é a dificuldade de gerenciar e incentivar de forma consistente as responsabilidades das crianças, ao mesmo tempo que lhes ensina educação financeira.

**Princípio Orientador:** Esta plataforma é uma aplicação nativa de IA de nova geração. Nosso objetivo principal é explorar as fronteiras da IA generativa, aproveitando-a para criar experiências de usuário inovadoras e inteligentes. Priorizaremos o uso de IA generativa para resolver problemas e construir funcionalidades sempre que viável.

## Objetivos

- **Objetivo Primário**: Alcançar um alto engajamento do usuário, medido pelo número de famílias ativas usando a plataforma mensalmente.
- **Objetivo Secundário**: Fomentar a responsabilidade nas crianças, medida pela taxa de tarefas concluídas.
- **Objetivo de Negócio**: Validar o modelo de monetização Freemium, convertendo usuários gratuitos em assinantes através de funcionalidades exclusivas (IA e Gift Cards).

**Referência**: Para detalhes completos sobre a estratégia de produto, proposta de valor e diferenciação entre planos Free vs Paid, consulte [Estratégia de Produto](../../docs/product_strategy.md).

## Histórias de Usuário

- **Como pai/mãe, eu quero...**
    - Registrar a mim e ao meu filho em uma plataforma web.
    - Definir um valor total de mesada mensal que desejo distribuir entre as tarefas do meu filho.
    - Criar tarefas de diferentes tipos: **diárias** (ex: "arrumar a cama"), **semanais** (ex: "ir ao curso de inglês"), ou **únicas** (ex: "passar na prova de matemática").
    - Atribuir um "Peso" ou "Importância" (Baixo, Médio, Alto) a cada tarefa para que o sistema possa calcular seu valor automaticamente.
    - Receber sugestões de uma IA para tarefas apropriadas para a idade.
    - Ser notificado quando uma tarefa for marcada como concluída e aprová-la. Para algumas tarefas, quero ver uma foto enviada pelo meu filho, que pode ser pré-validada por uma IA se eu for um assinante.
    - Criar um login simples (usuário/PIN) para meu filho acessar seu próprio portal.

- **Como filho(a), eu quero...**
    - Acessar um portal web simples com meu login para ver minhas tarefas.
    - Marcar uma tarefa como "concluída" através de um clique no portal.
    - Para tarefas que exigem prova, quero poder enviar uma foto através do portal.
    - Acompanhar quanto dinheiro ganhei com minhas tarefas concluídas.
    - Receber notificações sobre novas tarefas no WhatsApp (se meus pais configurarem).
    - **(Plano Pago)** Trocar meu saldo acumulado por Gift Cards (Roblox, iFood, etc.) diretamente no app.

## Funcionalidades Essenciais

1.  **Gerenciamento de Usuários**:
    - Registro seguro e gerenciamento de perfis para pais.
    - Criação de um perfil de filho associado, com credenciais de login simples (ex: usuário/PIN).

2.  **Gerenciamento de Tarefas com Múltiplos Tipos**:
    - **Tarefas Diárias**: Tarefas recorrentes que acontecem todos os dias.
    - **Tarefas Semanais**: Atividades agendadas para dias específicos da semana.
    - **Tarefas Únicas**: Metas de uma só vez ou eventos especiais.

3.  **Motor de Cálculo de Mesada**:
    - Os pais definem uma mesada mensal total.
    - Os pais atribuem um peso (ex: Baixo, Médio, Alto) a cada tarefa.
    - O sistema converte pesos em pontos (ex: Baixo=1, Médio=5, Alto=20).
    - A aplicação calcula o total de pontos possíveis em um mês e determina um valor "por ponto" dividindo a mesada total pelo total de pontos.
    - O valor de cada tarefa é calculado automaticamente com base em seus pontos.

4.  **Recursos com Inteligência Artificial (Plano Premium)**:
    - **Sugestão de Tarefas**: Um LLM fornece aos pais ideias de tarefas.
    - **Validação de Imagem**: Um LLM com capacidade de visão realiza uma verificação prévia nas fotos enviadas para confirmar se correspondem à tarefa concluída, sinalizando-a para a aprovação final do pai.

5.  **Fluxo de Conclusão**:
    - **Portal da Criança**: As crianças marcam as tarefas como concluídas através de seu painel web.
    - **Aprovação dos Pais**: Os pais aprovam as tarefas concluídas através do painel web principal.
    - **Notificações Opcionais via WhatsApp**: As crianças podem receber avisos sobre novas tarefas via WhatsApp, com um link para o portal.

6.  **Registro Financeiro**:
    - Um extrato simples e claro mostrando as tarefas concluídas e o valor da mesada ganha.

7.  **Planos e Monetização (Freemium)**:
    - **Plano Free**:
        - Limite de 5 tarefas recorrentes ativas.
        - Apenas 1 filho.
        - Aprovação de tarefas 100% manual.
        - Sem acesso a sugestões de tarefas por IA.
    - **Plano Pago (Premium)**:
        - Tarefas recorrentes ilimitadas.
        - **IA Generativa**: Sugestões de tarefas e Validação Visual de fotos.
        - **Loja de Recompensas**: Possibilidade de trocar saldo por Gift Cards reais.
        - Relatórios de comportamento via IA.

## Experiência do Usuário

- **Interface dos Pais**: Uma aplicação web abrangente e fácil de usar.
- **Interface das Crianças**: Um portal web dedicado, seguro e de fácil utilização. O WhatsApp pode ser usado como um canal de notificação opcional.

## Restrições Técnicas de Alto Nível

- A aplicação principal voltada para os pais deve ser baseada na web.
- Requer integração com um provedor de LLM de terceiros que ofereça capacidades de geração de texto e visão.
- **Ecossistema de Agentes de IA**: A arquitetura da solução deverá ser baseada em um ecossistema de agentes de IA, seguindo os seguintes padrões e tecnologias:
    - **Modelo de IA**: Utilizar a família de modelos **Google Gemini** como base para as capacidades de geração de texto e visão.
    - **Interface de Usuário para Agentes (Agent-UI)**: Adotar um framework dedicado para a construção de interfaces de usuário que interajam com os agentes, como o `CopilotKit` ou o protocolo `AG-UI`.
    - **Comunicação Agente-Ferramenta (MCP)**: Implementar o **Model Context Protocol (MCP)** para padronizar a forma como os agentes interagem com ferramentas externas, APIs e fontes de dados.
    - **Comunicação Agente-Agente (A2A)**: Utilizar um protocolo padrão para a comunicação entre agentes, como o **Agent-to-Agent Protocol (A2A)** ou o **Agent Communication Protocol (ACP)**, para permitir a colaboração e orquestração de tarefas complexas entre múltiplos agentes.

**Referências:**
- **Google Gemini:** [https://deepmind.google/technologies/gemini/](https://deepmind.google/technologies/gemini/)
- **Agent-to-Agent (A2A) Protocol:** [https://a2aprotocol.ai/](https://a2aprotocol.ai/)
- **Model Context Protocol (MCP) e ACP:** [https://agentcommunicationprotocol.dev/](https://agentcommunicationprotocol.dev/)

## Fora do Escopo (Não-Metas para o MVP)

- Transações bancárias diretas (PIX, TED) entre pais e filhos.
- Recursos de gamificação (emblemas, placares de líderes, etc.).
- Suporte para múltiplos filhos em uma única conta de pai/mãe.
- Relatórios e análises complexas para os pais.

## Plano de Lançamento em Fases

- **MVP**: Todos os recursos listados em "Funcionalidades Essenciais", com foco no portal da criança como interface primária. O objetivo é validar o ciclo central de criação de tarefas, conclusão e geração de valor.
- **Fase 2**: Explorar a integração de cartões pré-pagos para pagamentos no mundo real. Introduzir suporte para múltiplos filhos.

## Métricas de Sucesso

- **Métrica Principal**: Número de Famílias Ativas Mensalmente.
- **Métricas Chave**:
    - Taxa de conclusão de tarefas (Tarefas Concluídas / Tarefas Atribuídas).
    - Taxa de retenção de pais.

## Riscos e Mitigações

- **Risco Técnico**: A confiabilidade e o custo da API do LLM.
    - **Mitigação**: Selecionar provedores de API com SLAs claros e preços previsíveis. Projetar o sistema para lidar com interrupções de API de forma elegante.
- **Risco de Adoção pelo Usuário**: As crianças podem não se engajar com o sistema.
    - **Mitigação**: A plataforma oferece um portal web dedicado e simplificado como o principal meio de interação para a criança, o que remove barreiras de acesso (como a necessidade de ter WhatsApp). A interface deve ser extremamente intuitiva e focada na recompensa.
- **Risco de Precisão da IA**: A validação de imagem por IA pode produzir falsos positivos/negativos.
    - **Mitigação**: A validação da IA é uma verificação prévia, não uma aprovação final. O pai/mãe sempre tem a palavra final, o que minimiza o impacto de erros da IA.

## Questões em Aberto

- Como o sistema deve lidar com o número variável de dias e dias da semana em um mês ao calcular o "total de pontos possíveis"? (O cálculo deve ser dinâmâmico com base no mês do calendário).
- Qual será o processo exato e seguro para os pais criarem e gerenciarem as credenciais de login para o portal de seus filhos?
- Como a integração opcional com o WhatsApp será configurada e gerenciada pelos pais?