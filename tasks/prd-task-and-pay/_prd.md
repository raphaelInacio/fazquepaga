# Documento de Requisitos do Produto (PRD): TaskAndPay (Baseline Novembro 2025)

## Visão Geral

O TaskAndPay é uma plataforma SaaS para pais e filhos gerenciarem tarefas e mesadas. A plataforma permite aos pais atribuir valor monetário às atividades, acompanhar sua conclusão e automatizar o cálculo da mesada. O sistema utiliza IA para sugestões de tarefas e uma interface web para os pais, com o WhatsApp servindo como o principal canal de interação para os filhos.

Este documento serve como uma baseline, refletindo o estado atual da implementação e identificando o que foi concluído versus o que ainda está pendente.

**Princípio Orientador:** A plataforma é uma aplicação nativa de IA, priorizando o uso de IA generativa para criar experiências de usuário inovadoras.

## Objetivos

- **Objetivo Primário**: Alcançar alto engajamento do usuário, medido por famílias ativas.
- **Objetivo Secundário**: Fomentar a responsabilidade nas crianças, medida pela taxa de conclusão de tarefas.
- **Objetivo de Negócio**: Validar o modelo de monetização Freemium.

**Referência**: Para a estratégia de produto original, consulte [Estratégia de Produto](../../docs/product_strategy.md).

## Histórias de Usuário (Status Atual)

- **Como pai/mãe, eu quero...**
    - ✅ Registrar a mim e ao meu filho em uma plataforma web.
    - ✅ Definir um valor total de mesada mensal para meu filho.
    - ✅ Criar tarefas de diferentes tipos (diárias, semanais, únicas) com pesos (Baixo, Médio, Alto) para cálculo automático de valor.
    - ✅ Receber sugestões de tarefas de uma IA com base na idade.
    - 🟡 Ser notificado quando uma tarefa for marcada como concluída. *(A notificação existe, mas a aprovação via web não)*.
    - 🟡 Ver uma foto enviada pelo meu filho. *(O envio via WhatsApp é possível, mas a visualização e aprovação na interface web dos pais não está implementada)*.
    - ✅ Criar um login simples para meu filho (código de onboarding para WhatsApp).

- **Como filho(a), eu quero...**
    - ❌ Acessar um portal web simples com meu login para ver minhas tarefas. *(A interação atual é primariamente via WhatsApp)*.
    - ✅ Marcar uma tarefa como "concluída" via WhatsApp.
    - ✅ Enviar uma foto como prova via WhatsApp.
    - ❌ Acompanhar quanto dinheiro ganhei com minhas tarefas. *(O backend calcula, mas a interface para o filho não existe)*.
    - ✅ Receber notificações sobre novas tarefas no WhatsApp.
    - ✅ **(Plano Pago)** Trocar meu saldo acumulado por Gift Cards (Roblox, iFood, etc.) diretamente no app. *(Funcionalidade mockada disponível para pais Premium)*.

## Funcionalidades Essenciais (Status de Implementação)

| Funcionalidade | Status | Detalhes |
| :--- | :--- | :--- |
| **1. Gerenciamento de Usuários** | **Implementado** | Pais podem se registrar e adicionar filhos. A criança é integrada (onboarded) via WhatsApp. |
| **2. Gerenciamento de Tarefas** | **Parcialmente Implementado** | Pais podem criar e visualizar tarefas. A criação de tarefas respeita os limites do plano (Free/Premium). |
| **3. Motor de Cálculo de Mesada** | **Implementado** | O backend calcula o valor previsto da mesada com base nas tarefas e pesos definidos. |
| **4. Recursos com IA (Premium)** | **Parcialmente Implementado** | **Sugestão de Tarefas**: Implementado e funcional. **Validação de Imagem**: Backend está pronto para receber imagem e processar de forma assíncrona, mas o fluxo completo de aprovação não está finalizado. |
| **5. Fluxo de Conclusão** | **Parcialmente Implementado** | **Criança**: Pode submeter tarefas como concluídas via WhatsApp. **Pais**: **NÃO HÁ INTERFACE** para aprovar tarefas no portal web. Este é um GAP CRÍTICO. |
| **6. Registro Financeiro** | **Não Implementado** | Não há extrato financeiro visível para pais ou filhos. |
| **7. Planos e Monetização** | **Implementado** | A lógica de negócio para diferenciar os planos Free e Premium está implementada no backend (`SubscriptionService`), controlando o acesso a funcionalidades como IA, número de tarefas e Loja de Gift Cards. |
| **8. Loja de Gift Cards (Premium)** | **Implementado (Mock)** | Pais com plano Premium podem acessar uma loja de gift cards e "resgatar" itens. A funcionalidade é simulada. |

## Plano de Lançamento em Fases (Revisado)

- **MVP (Estado Atual)**: As funcionalidades essenciais para o pai (registro, criação de filho, criação de tarefas) e para o filho (conclusão via WhatsApp) estão implementadas. A monetização (planos e loja) está presente de forma lógica/mockada.
- **Próximos Passos para Concluir o MVP**:
    1. Implementar a interface de **aprovação de tarefas** para os pais no portal web.
    2. Implementar uma interface para os pais visualizarem a **prova (foto)** enviada pelo filho.
    3. Criar uma tela de **extrato financeiro** simples para os pais.
    4. Corrigir a dependência do frontend em `localStorage` criando endpoints de API para buscar dados de entidades (ex: `GET /api/v1/children/{id}`).

## Riscos e Mitigações

- **Risco de Adoção pelo Usuário**: A falta de um portal web para a criança pode limitar o engajamento de usuários que não usam ou não têm acesso fácil ao WhatsApp.
    - **Mitigação**: O foco no WhatsApp foi uma decisão de MVP, mas a criação de um portal web para a criança deve ser considerada na Fase 2.
- **Risco de Precisão da IA**: A validação de imagem por IA pode errar.
    - **Mitigação (Mantida)**: A IA atua como pré-validador; a aprovação final é (ou deveria ser) do pai.

## Questões em Aberto

- **Aprovação Web**: Como exatamente a interface de aprovação de tarefas para os pais deve funcionar no portal web?
- **Dependência do Frontend**: A dependência do frontend no `localStorage` para passar dados entre páginas é frágil. Devemos priorizar a criação de endpoints (`GET /api/v1/children/{id}`) para tornar a aplicação mais robusta?
- **Portal da Criança**: A interação via WhatsApp é suficiente para o MVP ou um portal web simples para a criança é necessário para o lançamento inicial?
- **Feedback de Conclusão**: Como o filho é notificado de que sua tarefa foi aprovada e o dinheiro creditado? Esse fluxo de feedback precisa ser definido.
