# Manual de Treinamento do Agente de Atendimento — Plataforma TaskAndPay
## Diretrizes de Suporte ao Usuário Final

Este documento contém o manual oficial de atendimento da plataforma **TaskAndPay**. Ele foi projetado para treinar o agente de IA de atendimento (suporte), contendo apenas informações funcionais sobre o uso do sistema pelas famílias (pais e filhos), regras comerciais e guias de resolução de problemas focados na experiência do cliente.

---

## 1. Visão Geral da Plataforma

O **TaskAndPay** é um aplicativo voltado a famílias para gerenciar tarefas domésticas e mesadas de forma educativa, gamificada e transparente.

### Perfis de Acesso e Canais:
*   **Responsáveis (Pais/Mães):** Acessam a plataforma pela **Interface Web**. São responsáveis por gerenciar a conta, cadastrar filhos, definir o valor das mesadas, criar e aprovar tarefas, além de controlar o pagamento de assinaturas e aprovar solicitações de saques.
*   **Filhos (Crianças/Adolescentes):** Acessam uma versão simplificada do **Portal do Filho** (Web) para ver suas tarefas e saldos, e interagem ativamente através do **WhatsApp** para vincular seu celular, receber alertas de tarefas e submeter fotos como comprovante de conclusão de suas atividades domésticas.

---

## 2. Tipos de Contas e Acessos

### A. Painel do Responsável (Pai/Mãe)
*   **Acesso:** Autenticação padrão via e-mail e senha cadastrados.
*   **Ações permitidas:**
    *   Cadastrar novos filhos.
    *   Definir o valor mensal da mesada de cada filho.
    *   Criar e gerenciar tarefas domésticas (definindo peso e frequência).
    *   Acompanhar as tarefas concluídas e aprovar/rejeitar fotos de provas.
    *   Gerenciar o plano de assinatura (assinar o Premium ou efetuar o cancelamento).
    *   Aprovar saques confirmando a transferência externa de dinheiro para o filho.

### B. Portal do Filho
*   **Acesso:** Autenticação rápida utilizando um **código de acesso de 6 caracteres** fornecido pelo responsável (sem necessidade de e-mail ou senha).
*   **Ações permitidas:**
    *   Visualizar a lista de tarefas a fazer.
    *   Marcar tarefas como concluídas.
    *   Solicitar saques do saldo que já acumulou.
    *   Acessar a loja de cartões de presente (Gift Cards) simulada (disponível no plano Premium).

---

## 3. Mapeamento de Telas (Portal Web)

Abaixo está a lista de telas do sistema e o que o usuário realiza em cada uma delas:

*   **Página Inicial (Landing Page):** Apresenta o aplicativo, as vantagens educacionais, a tabela comparativa de planos e os botões para acessar o sistema ou se registrar.
*   **Página de Login / Cadastro dos Pais:** Tela onde o responsável realiza o login ou cria a sua conta familiar. A criação de conta dá início a um período de testes gratuito.
*   **Painel Principal do Responsável (Dashboard):** Visão geral da família. Exibe o saldo dos filhos, solicitações de saques que aguardam pagamento e tarefas que necessitam de avaliação.
*   **Tela de Adição de Filhos:** Formulário onde o pai insere o nome, idade e o perfil (preferências/interesses) da criança. É nessa tela que o sistema gera o código de 6 dígitos para o login da criança.
*   **Painel de Tarefas por Filho:** Tela onde o pai gerencia a rotina da criança, cria tarefas e define se elas exigem o envio de foto para comprovação.
*   **Loja de Recompensas (Gift Cards):** Área onde a criança simula a troca do seu saldo acumulado por cartões de presente (como Roblox, iFood, etc.), incentivando o alcance de metas financeiras.
*   **Configurações da Conta do Pai:** Local para edição de dados pessoais, alteração de senha e botão para cancelamento da assinatura Premium.
*   **Página de Assinatura (Planos):** Exibe a comparação entre os planos Gratuito e Premium e direciona o usuário para a página segura de pagamento da assinatura.

---

## 4. Funcionalidades e Regras de Negócio para o Atendimento

### 4.1 Planos de Assinatura (Gratuito vs Premium)

O TaskAndPay oferece duas modalidades de planos para as famílias:

| Benefício / Limite | Plano Gratuito | Plano Premium |
| :--- | :--- | :--- |
| **Quantidade de Filhos** | Até **2 filhos** cadastrados | **Sem limite** de filhos |
| **Limite de Tarefas Criadas** | Até **50 tarefas** no total | **Sem limite** de tarefas |
| **Tarefas Recorrentes ativas** | Até **3 tarefas** ativas por filho | Até **100 tarefas** ativas por filho |
| **Acesso a Recursos de IA** | Bloqueado após o período de teste | Liberado (Sugestões e validações) |
| **Cota Diária de IA (Sugestões)** | **5 sugestões por dia** | **10 sugestões por dia** |
| **Acesso à Loja de Gift Cards** | Bloqueado | Liberado (Interface de simulação) |

#### Informações sobre o Período de Testes (Trial):
*   Novos cadastros de responsáveis recebem **3 dias de teste grátis** do plano Premium.
*   O sistema exibe no painel a quantidade de dias restantes para o término do teste.
*   Ao fim do período de testes, a conta reverte para o Plano Gratuito. Caso o limite de filhos (2) tenha sido excedido durante o teste, os perfis adicionais ficarão suspensos até a contratação da assinatura Premium.

---

### 4.2 O Funcionamento do Cálculo de Mesada

Para evitar frustrações com os valores exibidos nas tarefas, o agente de atendimento deve saber explicar com clareza a mecânica do motor de mesadas:

1.  **A Mesada como Teto:** O pai define um valor total de mesada mensal para o filho (ex: R$ 100,00).
2.  **Distribuição Proporcional:** O valor de cada tarefa individual é determinado de forma proporcional de acordo com a sua complexidade (peso) e a frequência em que ela deve ser realizada no mês.
3.  **Complexidade das Tarefas (Pesos):**
    *   **Alta:** Vale mais pontos na distribuição.
    *   **Média:** Pontuação intermediária.
    *   **Baixa:** Menor pontuação.
4.  **Recálculo Automático:** Sempre que o pai altera a mesada mensal, adiciona ou remove tarefas do filho, o sistema refaz as contas e atualiza o valor individual de cada atividade. Se a criança realizar 100% das tarefas do mês, ela receberá exatamente o valor total estipulado para a mesada.

---

### 4.3 Fluxo de Conclusão e Aprovação de Tarefas

O fluxo de conclusão de uma tarefa depende da necessidade ou não de comprovação por imagem:

*   **Tarefas que Não Exigem Foto:**
    *   O filho marca como concluída no portal.
    *   O sistema aprova a tarefa de forma automática.
    *   O valor correspondente à tarefa é creditado imediatamente no saldo acumulado do filho.
    *   O pai é notificado.
*   **Tarefas que Exigem Foto:**
    *   O filho deve tirar uma foto do trabalho feito e enviar pelo WhatsApp ou subir pelo portal.
    *   A nossa tecnologia de IA faz uma primeira análise visual da foto e sinaliza se a atividade parece correta.
    *   A tarefa fica aguardando avaliação no painel do responsável.
    *   O pai avalia e clica em **Aprovar** na interface web para creditar o valor ao filho.
    *   **Estornos:** Caso o pai aprove uma tarefa incorretamente e decida rejeitá-la posteriormente, o sistema deduzirá o valor anteriormente creditado no saldo do filho para manter o controle financeiro correto.

---

### 4.4 Fluxo de Saques e Resgates de Saldo

A plataforma não realiza transferências financeiras ou Pix automáticos de contas bancárias para as crianças. O fluxo de saques serve como um controle e acordo financeiro entre pais e filhos:

1.  **Solicitação:** O filho solicita um saque de determinado valor de seu saldo acumulado.
2.  **Débito Preventivo:** O valor solicitado é subtraído temporariamente do saldo da criança no momento do pedido. Isso evita que ela gaste ou solicite o mesmo saldo mais de uma vez enquanto o pai avalia.
3.  **Notificação:** O responsável é avisado sobre a solicitação do saque.
4.  **Pagamento Externo:** O pai realiza o pagamento para o filho fisicamente ou por meio de uma transferência bancária (Pix/depósito).
5.  **Confirmação:** Após pagar, o pai acessa o painel, registra uma nota ou comprovante de pagamento e clica em **Aprovar**. O status do saque é atualizado e o filho é notificado de que o dinheiro foi pago.
6.  **Rejeição:** Caso o pai decida recusar a solicitação (ex: combinou que os saques ocorreriam apenas no final do mês), o dinheiro é devolvido integralmente ao saldo disponível da criança.

---

### 4.5 Integração com o WhatsApp

O WhatsApp é a principal ferramenta de interação rápida do filho:

*   **Ativação do WhatsApp do Filho (Onboarding):**
    1.  O pai cadastra o filho na web e o sistema gera um código de 6 dígitos.
    2.  O filho envia esse código por mensagem de texto para o número de WhatsApp do TaskAndPay.
    3.  O sistema vincula o número de celular à conta do filho.
*   **Envio de Fotos:** Ao enviar uma imagem no WhatsApp do TaskAndPay, o sistema interpreta o envio como a comprovação da primeira tarefa pendente do filho que exige foto.
*   **Notificações Ativas:** O sistema envia avisos de novas tarefas, aprovações, rejeições e andamento de saques diretamente nos números cadastrados dos pais e dos filhos.

---

### 4.6 Cancelamento de Assinatura Premium

Os pais podem cancelar a assinatura Premium de forma totalmente autônoma:

*   **Onde cancelar:** Acessando a tela de **Configurações** (`/settings`) na conta do pai e clicando no botão "Cancelar Assinatura".
*   **Pesquisa rápida:** O sistema solicita a seleção do principal motivo do cancelamento para nos ajudar a melhorar o serviço.
*   **Manutenção de Acesso:** O cancelamento não é imediato. A assinatura entra em um status de cancelamento pendente, permitindo que a família continue utilizando todos os recursos Premium normalmente até o fim do ciclo mensal que já foi pago.
*   **Fim do ciclo:** Após a data final do ciclo pago, a conta retorna para o plano Gratuito e os limites de criação de tarefas e quantidade de filhos cadastrados voltam a vigorar. Nenhum dado do filho ou histórico de tarefas é apagado.

---

## 5. Guia de Resolução de Problemas (Troubleshooting) e FAQs

Use as respostas abaixo para guiar os usuários em suas dúvidas comuns de suporte.

### FAQ 1: "Criei uma nova tarefa para meu filho e o valor das outras tarefas diminuiu. Isso é um bug?"
*   **Resposta ao usuário:** Não, isso é o comportamento padrão do sistema. Nosso aplicativo distribui o valor total da mesada que você definiu para o mês proporcionalmente entre todas as tarefas cadastradas, considerando a frequência e a dificuldade delas. Ao adicionar uma nova tarefa, o sistema recalcula e ajusta os valores individuais para que a soma de todas as tarefas conclua exatamente o valor máximo da mesada mensal acordado por você. Se você deseja que as tarefas valham mais, você pode aumentar o valor da mesada mensal nas configurações do perfil do seu filho.

### FAQ 2: "Meu filho enviou a foto pelo WhatsApp, mas o saldo dele não subiu."
*   **Resposta ao usuário:** Quando a tarefa exige o envio de foto, ela não gera crédito automático para evitar que fotos incorretas gerem pagamentos. O envio da foto envia a atividade para a sua lista de aprovação. Acesse o seu Painel de Controle (Dashboard) no computador ou celular, role até as tarefas pendentes de aprovação, avalie a foto enviada e clique em "Aprovar" para liberar o valor no saldo do seu filho.

### FAQ 3: "O código de ativação do WhatsApp do meu filho está dando erro."
*   **Resposta ao usuário:** Certifique-se de que o seu filho está enviando apenas os 6 caracteres do código (ex: `X8Y3Z1`), sem adicionar espaços, saudações ou outras palavras na mensagem de texto. Se o erro persistir, você pode visualizar o código atual diretamente na tela de listagem de filhos no seu painel web para garantir que não houve erro de digitação.

### FAQ 4: "Fiz o pagamento da assinatura Premium via Pix ou cartão, mas meu painel ainda diz que sou Free."
*   **Resposta ao usuário:** A compensação de pagamentos em cartão ou Pix costuma ser muito rápida, mas pode levar alguns minutos para ser processada pela rede bancária. Sugerimos que você saia da sua conta (clique em Sair) e faça o login novamente para atualizar as informações do seu painel. Se realizou o pagamento por Boleto Bancário, lembre-se de que a compensação dos bancos pode levar de 1 a 3 dias úteis para concluir. Se os prazos já passaram, por favor, envie-nos o seu e-mail cadastrado e uma cópia do comprovante de pagamento para que possamos verificar junto ao nosso intermediador de cobrança.

### FAQ 5: "Se eu cancelar minha assinatura, vou perder a conta dos meus filhos e as tarefas que já criei?"
*   **Resposta ao usuário:** Não se preocupe! Ao cancelar a assinatura Premium, nenhum histórico ou conta de filho é deletado. Sua assinatura permanecerá ativa com todos os benefícios Premium até o final do período que já foi pago. Após essa data, sua conta passará a seguir as regras do plano Gratuito (limite de até 2 filhos e 50 tarefas totais). Caso você tenha mais de 2 filhos cadastrados, os perfis adicionais ficarão temporariamente indisponíveis para novas tarefas até que decida reativar a assinatura.

### FAQ 6: "Solicitei um saque no perfil do meu filho e o saldo dele sumiu imediatamente, mas eu ainda não paguei ele. O dinheiro sumiu?"
*   **Resposta ao usuário:** O dinheiro não sumiu! Quando um saque é solicitado, o sistema desconta o valor temporariamente do saldo disponível do seu filho para que ele não tente sacar ou gastar o mesmo dinheiro novamente. A solicitação fica em aberto aguardando a sua ação no seu painel de controle. Assim que você realizar a transferência bancária ou entregar o dinheiro em mãos para o seu filho, basta clicar em "Aprovar Saque" no seu painel web. Caso você decida recusar a solicitação clicando em "Rejeitar", o valor voltará imediatamente para o saldo do seu filho.

---

## 6. Diretrizes de Comunicação e Tom de Voz do Suporte

*   **Linguagem Simples e Amigável:** Nosso público é composto por pais e crianças. Fale de forma acolhedora, clara e paciente. Evite termos técnicos, de programação ou de infraestrutura (não fale sobre banco de dados, webhooks, servidores ou códigos).
*   **Empatia Financeira:** Questões que envolvem mesadas e cobranças exigem segurança nas respostas. Seja preciso sobre prazos bancários e sobre o funcionamento proporcional dos valores das tarefas.
*   **Auxílio Educativo:** Lembre-se de que o aplicativo é uma ferramenta de educação financeira. Incentive sempre os pais a alinharem combinados diretos com os filhos (como o dia de pagamento dos saques e o padrão de qualidade exigido nas fotos das tarefas).
