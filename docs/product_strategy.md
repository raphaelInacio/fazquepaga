# Estratégia de Produto: Proposta de Valor e Planos (Free vs Paid)

## 1. Proposta de Valor Central (O "Porquê")
O **TaskAndPay** resolve o conflito entre pais e filhos sobre responsabilidades e dinheiro.
- **Para os Pais**: Automatiza a "negociação" da mesada, ensina educação financeira e garante que as tarefas sejam feitas sem microgerenciamento constante.
- **Para os Filhos**: Transforma obrigações chatas em um "jogo" com recompensas claras e tangíveis (Gift Cards), dando autonomia e senso de conquista.

---

## 2. Estrutura dos Planos

O objetivo é que o plano **Free** seja útil o suficiente para criar o hábito (retenção), mas que o plano **Paid** ofereça conveniência e "superpoderes" irresistíveis.

### Plano Free (O "Gancho")
*Focado em: Organização Básica e Registro.*

*   **Gestão de Tarefas Simples**:
    *   Criação de tarefas manuais (Avulsas e Diárias).
    *   Limite de tarefas ativas (ex: até 5 tarefas recorrentes).
    *   1 Filho.
*   **Cálculo de Mesada**:
    *   Motor de cálculo básico (Pontos -> Valor).
    *   Extrato simples.
*   **Integração WhatsApp**:
    *   Filho recebe tarefas e envia fotos.
    *   **Aprovação 100% Manual**: O pai/mãe deve olhar a foto e aprovar no painel. A IA não pré-valida.
*   **Valor Entregue**: Elimina o "caderninho" ou a planilha de mesada. Centraliza a comunicação.

### Plano Pago (O "Superpoder")
*Focado em: Automação, Inteligência e Recompensas Reais.*

#### A. Gestão de Tarefas Avançada & IA ("Gestão das Tarefas")
*   **Tarefas Ilimitadas**: Sem limite de tarefas recorrentes.
*   **Sugestões de IA**: "O que meu filho de 10 anos deveria estar fazendo?" (IA sugere tarefas baseadas na idade).
*   **Validação Visual por IA**: A IA analisa a foto enviada pelo filho e diz "Parece que a cama está arrumada" (Economiza tempo do pai na aprovação).
*   **Relatórios Inteligentes**: Resumos semanais via WhatsApp ou E-mail: "Seu filho completou 80% das tarefas esta semana. Destaque: Melhorou na arrumação do quarto."

#### B. Conversão de Mesada em Gift Cards ("Oferta Irresistível")
*   **Loja de Recompensas**: O saldo acumulado pode ser trocado *diretamente* no app por Gift Cards (Roblox, iFood, Uber, PlayStation, Xbox).
*   **Por que é pago?**: Envolve taxas de transação e parcerias. É o grande motivador para a criança pedir aos pais para assinarem.

#### C. Funcionalidades Futuras (Retenção)
*   Múltiplos Filhos.
*   Boost de Mesada (Tarefas extras com valor dinâmico).

---

## 3. Tabela Comparativa

| Funcionalidade | Plano Free | Plano Pago (Premium) |
| :--- | :--- | :--- |
| **Filhos** | 1 Filho | Ilimitado (Futuro) |
| **Tarefas** | Limite de 5 recorrentes | Ilimitadas |
| **Tipos de Tarefa** | Avulsas, Diárias | Todas (+ Semanais, Mensais) |
| **Aprovação** | Manual | **Pré-validada por IA** (Visão) |
| **Sugestões de Tarefas** | Não | **IA Generativa** |
| **Relatórios** | Extrato Simples | **Resumos de IA** (Comportamento) |
| **Uso da Mesada** | Apenas registro (Pai paga por fora) | **Troca por Gift Cards no App** |

## 4. Próximos Passos de Implementação

1.  **Backend**: Adicionar campo `subscriptionTier` (FREE, PAID) ao modelo de `User` ou `Family`.
2.  **Feature Flags**: Implementar verificação de limites (ex: `canCreateTask()`, `canUseAI()`).
3.  **Frontend**:
    *   Criar "Paywall" visual nas features Premium (botão de "Gerar Sugestão com IA" bloqueado com cadeado).
    *   Criar página de "Loja" (Mockup inicial) para os Gift Cards.
