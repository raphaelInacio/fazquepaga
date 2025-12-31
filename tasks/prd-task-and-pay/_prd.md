# Documento de Requisitos do Produto (PRD): TaskAndPay (Baseline Novembro 2025)

## Visão Geral

O TaskAndPay é uma plataforma SaaS para pais e filhos gerenciarem tarefas e mesadas. A plataforma permite aos pais atribuir valor monetário às atividades, acompanhar sua conclusão e automatizar o cálculo da mesada. O sistema utiliza IA para sugestões de tarefas e uma interface web para os pais, com o WhatsApp servindo como o principal canal de interação para os filhos.

Este documento serve como uma baseline, refletindo o estado atual da implementação e identificando o que foi concluído versus o que ainda está pendente.

**Princípio Orientador:** A plataforma é uma aplicação nativa de IA, priorizando o uso de IA generativa para criar experiências de usuário inovadoras.

## Objetivos

- **Objetivo Primário**: Alcançar alto engajamento do usuário, medido por famílias ativas.
- **Objetivo Secundário**: Fomentar a responsabilidade e educação financeira nas crianças.
- **Objetivo de Negócio**: Validar e expandir o modelo de monetização Free Trial + Assinatura Premium.

**Referência**: Para a estratégia de produto original, consulte [Estratégia de Produto](../../docs/product_strategy.md).

## Histórias de Usuário (Status Atual)

- **Como pai/mãe, eu quero...**
    - ✅ Registrar a mim e ao meu filho em uma plataforma web.
    - ✅ Definir um valor total de mesada mensal para meu filho.
    - ✅ Criar tarefas de diferentes tipos (diárias, semanais, únicas) com pesos (Baixo, Médio, Alto) para cálculo automático de valor.
    - ✅ Receber sugestões de tarefas de uma IA com base na idade.
    - ✅ Ser notificado quando uma tarefa for marcada como concluída.
    - ✅ Aprovar tarefas e ver provas enviadas pelo filho na interface web.
    - ✅ Ver uma foto enviada pelo meu filho via WhatsApp na interface web.
    - ✅ Criar um login simples para meu filho (código de onboarding para WhatsApp).
    - 🆕 **(Assinatura)** Quero assinar o plano Premium pagando via Asaas (cartão/boleto/pix) para liberar recursos exclusivos.
    - 🆕 **(IA)** Quero definir um perfil comportamental (Bio) do meu filho para que a IA sugira tarefas mais adequadas.
    - 🆕 **(Saque)** Quero receber uma notificação no WhatsApp quando meu filho solicitar um saque.
    - 🆕 **(Saque)** Quero aprovar um saque e marcar como "Pago" manualmente após transferir o dinheiro.

- **Como filho(a), eu quero...**
    - ✅ Acessar um portal web simples com meu login para ver minhas tarefas.
    - ✅ Marcar uma tarefa como "concluída" via WhatsApp ou Portal Web.
    - ✅ Enviar uma foto como prova via WhatsApp.
    - ⚠️ Acompanhar quanto dinheiro ganhei com minhas tarefas. *(Disponível para pais, em breve para filhos com Coach Financeiro)*.
    - ✅ Receber notificações sobre novas tarefas no WhatsApp.
    - ✅ **(Plano Pago)** Trocar meu saldo acumulado por Gift Cards (Roblox, iFood, etc.) diretamente no app. *(Funcionalidade mockada disponível para pais Premium)*.
    - 🆕 **(Saque)** Quero solicitar o saque do meu saldo acumulado.
    - 🆕 **(Saque)** Quero ser avisado no WhatsApp quando meu saque for aprovado/pago.

## Funcionalidades Essenciais (Status de Implementação)

| Funcionalidade | Status | Detalhes |
| :--- | :--- | :--- |
| **1. Gerenciamento de Usuários** | **Implementado** | Pais podem se registrar e adicionar filhos. A criança é integrada (onboarded) via WhatsApp. |
| **2. Gerenciamento de Tarefas** | **Parcialmente Implementado** | Pais podem criar e visualizar tarefas. A criação de tarefas respeita os limites do plano (Free/Premium). |
| **3. Motor de Cálculo de Mesada** | **Implementado** | O backend calcula o valor previsto da mesada com base nas tarefas e pesos definidos. |
| **4. Recursos com IA (Premium)** | **Parcialmente Implementado** | **Sugestão de Tarefas**: Implementado e funcional. **Validação de Imagem**: Backend está pronto para receber imagem e processar de forma assíncrona, mas o fluxo completo de aprovação não está finalizado. |
| **5. Fluxo de Conclusão** | **Implementado** | **Criança**: Submete via WhatsApp ou Portal. **Pais**: Aprovam via Dashboard Web. |
| **6. Registro Financeiro** | **Implementado** | Extrato financeiro (Ledger) disponível para pais. |
| **7. Planos e Monetização** | **Implementado** | Integração com **Asaas** para gestão de assinaturas (Checkout transparente ou Link). Controla acesso a funcionalidades Premium. |
| **8. Loja de Gift Cards (Premium)** | **Implementado (Mock)** | Pais com plano Premium podem acessar uma loja de gift cards e "resgatar" itens. A funcionalidade é simulada. |
| **9. Controle de Saque** | **Planejado** | Fluxo: Solicitação (Filho) -> Notificação (Pai) -> Pagamento Externo -> Baixa manual no sistema (Pai). (Sem custódia de valores). |
| **10. AI Context** | **Planejado** | Cadastro de "Bio/Interesses" da criança para personalizar sugestões de tarefas. |

## Fluxo de Notificações (WhatsApp)

| Evento | Destinatário | Conteúdo |
| :--- | :--- | :--- |
| **Tarefa Concluída** | Pai/Mãe | "João terminou 'Lavar a louça'. Aprove agora!" + Link/Foto |
| **Tarefa Aprovada** | Filho | "Parabéns! Você ganhou R$ 5,00." |
| **Tarefa Rejeitada** | Filho | "Sua tarefa precisa de revisão: 'Faltou secar'." |
| **Solicitação de Saque** | Pai/Mãe | "João quer sacar R$ 50,00." |
| **Saque Pago** | Filho | "Seu saque de R$ 50,00 foi pago!" |

## AI Roadmap & Funcionalidades Futuras

Para aprofundar nosso diferencial como uma plataforma nativa de IA, as seguintes funcionalidades estão planejadas para evoluções futuras do produto.

### Aprimoramentos com IA (Próximas Fases)

- **Coach Financeiro para Crianças:** No portal infantil, um assistente de IA ajudará a criança a definir metas de economia (ex: "Quero um jogo de R$250") e criará um plano de tarefas e economia para alcançar esse objetivo, com estímulos visuais e sugestões de tarefas extras.
- **"Modo Aventura" para Tarefas:** Uma opção para a criança gamificar sua experiência, onde a IA reescreve as tarefas com uma temática lúdica e oferece feedback divertido.
- **Insights para os Pais no Extrato:** O extrato financeiro será enriquecido com dicas e observações geradas por IA sobre os hábitos financeiros da criança.
- **"Pacotes de Tarefas" por Objetivo:** Pais poderão pedir à IA para gerar um conjunto de tarefas com base em um objetivo de desenvolvimento (ex: "ensinar responsabilidade na cozinha").

### Visão de Longo Prazo (Future)

- **Motor de Mesada Inteligente:** Substituir os pesos manuais por uma sugestão de valor monetário gerado por IA, baseado na tarefa, idade e outros dados.
- **Relatórios Preditivos e de Comportamento:** Análise de padrões de conclusão de tarefas para fornecer aos pais insights mais profundos e sugestões proativas para ajudar a criança.
- **Notificações Inteligentes:** Notificações contextuais e personalizadas para pais e filhos, com textos gerados por IA para aumentar o engajamento.

## Plano de Lançamento em Fases (Revisado)

- **MVP (Estado Atual)**: Funcionalidades essenciais completas, incluindo Portal da Criança e Aprovação dos Pais.
- **Próximos Passos (Fase 2)**:
    1. Implementar **Coach Financeiro** para crianças.
    2. Expandir **Insights de IA** no extrato financeiro.
    3. Lançar a **v1 do Portal da Criança** com o "Modo Aventura" e o "Coach Financeiro".
    4. Implementar a geração de **"Pacotes de Tarefas"**.

## Riscos e Mitigações e Questões em Aberto
*(Seções mantidas como na versão anterior)*