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
    - ✅ **(Cancelamento de Assinatura)** Quero cancelar minha assinatura de forma autônoma via interface web, sendo informado das perdas de recursos Premium e fornecendo o motivo para ajudar a melhorar o produto.
    - ✅ **(Cancelamento de Assinatura)** Quero receber confirmação do cancelamento via WhatsApp para ter registro da ação.
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
    - 🆕 **(Saque)** Quero ser avisado no WhatsApp quando meu saque for aprovado/pago.

- **Como operador/desenvolvedor da plataforma, eu quero...**
    - ✅ Limitar o número de requisições por usuário/IP para evitar abuso e controlar custos (Rate Limiting).
    - ✅ Ter quotas diárias de uso de IA por usuário (Free/Premium) para evitar surpresas na fatura.
    - ✅ Garantir que secrets de autenticação estejam seguros no GCP Secret Manager (Zero secrets no código).
    - ✅ Bloquear tráfego automatizado (Bots) em endpoints de login/registro.

## Funcionalidades Essenciais (Status de Implementação)

| Funcionalidade | Status | Detalhes |
| :--- | :--- | :--- |
| **1. Gerenciamento de Usuários** | **Implementado** | Pais podem se registrar e adicionar filhos. A criança é integrada (onboarded) via WhatsApp. |
| **2. Gerenciamento de Tarefas** | **Parcialmente Implementado** | Pais podem criar e visualizar tarefas. A criação de tarefas respeita os limites do plano (Free/Premium). |
| **3. Motor de Cálculo de Mesada** | **Implementado** | O backend calcula o valor previsto da mesada com base nas tarefas e pesos definidos. |
| **4. Recursos com IA (Premium)** | **Implementado** | **Sugestão de Tarefas (Quota)**: Funcional com limite diário (5 Free / 50 Premium). **Validação de Imagem (Quota)**: Processamento assíncrono com limite de uso. |
| **5. Fluxo de Conclusão** | **Implementado** | **Criança**: Submete via WhatsApp ou Portal. **Pais**: Aprovam via Dashboard Web. |
| **6. Registro Financeiro** | **Implementado** | Extrato financeiro (Ledger) disponível para pais. |
| **7. Planos e Monetização** | **Implementado** | Integração com **Asaas** para gestão de assinaturas. Controla acesso a funcionalidades Premium e Quotas de IA. |
| **8. Loja de Gift Cards (Premium)** | **Implementado (Mock)** | Pais com plano Premium podem acessar uma loja de gift cards e "resgatar" itens. A funcionalidade é simulada. |
| **9. Controle de Saque** | **Planejado** | Fluxo: Solicitação (Filho) -> Notificação (Pai) -> Pagamento Externo -> Baixa manual no sistema (Pai). |
| **10. AI Context** | **Planejado** | Cadastro de "Bio/Interesses" da criança para personalizar sugestões de tarefas. |
| **11. Segurança e Proteção** | **Implementado** | **Rate Limiting**: In-memory (Caffeine) global e por endpoint. **Bot Protection**: reCAPTCHA v3 no login/registro. **Hardening**: Secrets no GCP Secret Manager e Refresh Tokens implementados. |
| **12. Cancelamento de Assinatura** | **Implementado** | Fluxo self-service de cancelamento Premium via interface web com pesquisa de churn e notificação no WhatsApp. |


## Fluxo de Notificações (WhatsApp)

| Evento | Destinatário | Conteúdo |
| :--- | :--- | :--- |
| **Tarefa Concluída** | Pai/Mãe | "João terminou 'Lavar a louça'. Aprove agora!" + Link/Foto |
| **Tarefa Aprovada** | Filho | "Parabéns! Você ganhou R$ 5,00." |
| **Tarefa Rejeitada** | Filho | "Sua tarefa precisa de revisão: 'Faltou secar'." |
| **Solicitação de Saque** | Pai/Mãe | "João quer sacar R$ 50,00." |
| **Saque Pago** | Filho | "Seu saque de R$ 50,00 foi pago!" |
| **Assinatura Cancelada** | Pai/Mãe | Confirmação de cancelamento e data até quando o Premium é mantido |

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

---

## Detalhamento: Cancelamento de Assinatura (Premium)

### Funcionalidades do Cancelamento

#### FR-1: Interface de Cancelamento
* **Descrição**: Adiciona o botão "Cancelar Assinatura" na página de configurações (Settings) para usuários Premium.
* **Requisitos**:
  1. Exibir botão apenas para usuários Premium ativos.
  2. Botão deve abrir modal com fluxo de cancelamento.
  3. Estilo visual de ação destrutiva (vermelho).

#### FR-2: Coleta de Motivo (Churn Survey)
* **Descrição**: Apresenta opções de motivo para cancelamento.
* **Requisitos**:
  1. Opções pré-definidas: "Muito caro", "Não uso os recursos Premium", "Encontrei alternativa melhor", "Vou voltar depois", "Outro" (habilita campo de texto livre).
  2. Seleção de motivo obrigatória para prosseguir.
  3. Armazenar motivo no Firestore para análise de churn.

#### FR-3: Tela de Confirmação com Impacto
* **Descrição**: Exibe resumo das perdas decorrentes do cancelamento antes da confirmação final.
* **Requisitos**:
  1. Exibir recursos a serem perdidos (limite de filhos: ilimitado → 1, tarefas recorrentes: ilimitado → 5, acesso à IA perdido, loja de Gift Cards perdida).
  2. Exibir a data até quando o acesso Premium será mantido.
  3. Botões de "Confirmar Cancelamento" e de cancelamento da ação (voltar).

#### FR-4: Processamento do Cancelamento
* **Descrição**: Processa o cancelamento na API Asaas e atualiza estado local.
* **Requisitos**:
  1. Chamar `DELETE /v3/subscriptions/{id}` do Asaas com o subscriptionId do usuário.
  2. Atualizar status local para `PENDING_CANCELLATION` (manter tier PREMIUM até fim do período pago).
  3. Registrar data e motivo no Firestore.
  4. Tratar erros de API com feedback amigável ao usuário.

#### FR-5: Notificação de Confirmação
* **Descrição**: Envia notificação via WhatsApp confirmando o cancelamento.
* **Requisitos**:
  1. Enviar mensagem para o número cadastrado do pai/mãe.
  2. Mensagem com confirmação e data até quando o acesso Premium será mantido.

### Experiência do Usuário (UX)
* **Fluxo**: Settings → Botão "Cancelar Assinatura" → Modal: Motivo → Modal: Confirmação → Sucesso.
* **Estilo**: Modal de motivo neutro; modal de confirmação com aviso de warning (laranja/amarelo) e botão final em vermelho.
* **Acessibilidade**: Modais navegáveis por teclado e aria-labels corretos.

### Restrições Técnicas
* **API Asaas**: DELETE na rota `/v3/subscriptions/{id}`.
* **Notificação**: Twilio WhatsApp (infraestrutura existente).
* **Persistência**: Firestore para armazenar campos `cancellationDate` e `cancellationReason` na coleção de usuários.
* **Webhook**: Sincronização via webhook Asaas para atualizar status de cancelamento efetivo.

### Non-Goals (Fora de Escopo)
* Reembolso proporcional do período não utilizado.
* Opção de pausar assinatura temporariamente.
* Ofertas de retenção personalizadas no fluxo.
* Cancelamento via interface do WhatsApp.
* Reativação automática da assinatura cancelada.