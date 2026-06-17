# PRD: Integração de Gift Cards via RV Hub

## Visão Geral

Este documento detalha os requisitos para a integração da venda e resgate de Gift Cards (vouchers digitais) no TaskAndPay utilizando a API do parceiro RV Hub. O objetivo é permitir que dependentes utilizem seu saldo fictício (recebido através de mesadas ou tarefas) para "comprar" vouchers de serviços que consomem no dia a dia, como Roblox, PlayStation, Uber e iFood. Como o saldo da plataforma é meramente educativo/fictício, a efetivação real da compra do PIN no RV Hub estará condicionada a uma autorização e cobrança real no cartão de crédito dos pais (Responsáveis).

## Objetivos

- **Engajar Dependentes:** Fornecer um catálogo atrativo e real de recompensas onde possam gastar seu saldo.
- **Educação Financeira:** Demonstrar aos dependentes a equivalência de seus esforços/tarefas com bens e serviços reais.
- **Controle dos Pais:** Garantir que 100% das emissões de vouchers passem pela aprovação manual do responsável financeiro.
- **Monetização/Cobrança:** Assegurar a cobrança via cartão de crédito do pai *antes* de confirmar a transação na RV Hub, já que o saldo do app não possui lastro bancário automático.

## Histórias de Usuário

- **Como dependente**, eu quero visualizar um catálogo organizado de Gift Cards separados por categorias (Jogos, Delivery, Streaming) para que eu encontre facilmente o que quero resgatar com minha mesada.
- **Como dependente**, eu quero adicionar um Gift Card a uma lista de desejos (carrinho de resgate) para enviar uma solicitação ao meu pai/mãe.
- **Como pai/mãe**, eu quero receber uma notificação de solicitação de resgate do meu filho para revisar o pedido.
- **Como pai/mãe**, eu quero aprovar a solicitação do meu filho, permitindo que o aplicativo realize a cobrança no meu cartão de crédito cadastrado e então emita o voucher para ele.
- **Como dependente**, eu quero visualizar o código PIN gerado logo após a compra ser aprovada pelo meu responsável.

## Funcionalidades Principais

1. **Vitrine Curada de Gift Cards (Recarga de PIN)**
   - O aplicativo deverá apresentar uma lista limitada e curada de categorias de Gift Cards, obtidas do RV Hub. 
   - Apenas categorias atrativas para o público alvo (ex: Xbox, Roblox, iFood, Uber) serão listadas. Recargas de celular (TIM, Vivo, Claro) não estarão disponíveis.
2. **Carrinho de Solicitação (Dependente)**
   - Mecanismo onde o dependente usa o saldo fictício da plataforma para reservar o pedido do Gift Card. Ao submeter, o pedido fica em status `Pendente de Aprovação`.
3. **Fluxo de Aprovação e Pagamento (Responsável)**
   - Os pais recebem o pedido pendente. 
   - Ao clicar em "Aprovar", o sistema **exige a confirmação e efetua o processamento da compra no cartão de crédito do pai**.
   - Se a transação do cartão de crédito for bem-sucedida, o sistema debita o saldo fictício do dependente.
4. **Integração com RV Hub (Backend)**
   - Imediatamente após a captura do pagamento no cartão de crédito do pai, o sistema deve solicitar o voucher no RV Hub através das rotas de `Recarga de PIN` (`/pin-topups/transactions` e `/capture`).
   - O PIN retornado deve ser armazenado e exibido de forma segura no perfil do dependente.

## Experiência do Usuário

- **Personas:** 
  - **Criança/Adolescente:** Quer uma interface visual, categorizada com logos grandes dos jogos/serviços que eles mais gostam. 
  - **Pais:** Quer clareza. Ao aprovar um pedido, a interface deve exibir um *Warning* explícito: "Esta ação efetuará uma cobrança de R$ XX,XX no seu cartão de crédito cadastrado. O saldo do aplicativo de seu filho é apenas educativo e não cobre os custos desta operação."
- **UX Geral:** 
  - Catálogo dividido em sessões com "cards" atrativos.
  - Tela de acompanhamento de status do pedido para os filhos ("Aguardando papai/mamãe", "Aprovado", "PIN Gerado").

## Restrições Técnicas de Alto Nível

- **Fluxo de Integração:** O motor de compras deve garantir uma arquitetura que evite perda financeira. Exigência de implementação do cabeçalho `X-Idempotency-Key` (Idempotência) suportado pela RV Hub, para assegurar que falhas de rede durante o processo não gerem compras repetidas no RV Hub após cobrança única no cartão de crédito do pai.
- **Integrações Externas:** 
  - API do Gateway de Pagamento atual do projeto (provavelmente Asaas) para processar o cartão dos pais.
  - API REST da RV Hub para emissão e captura de "Recargas de PIN".
- **Ambiente e Chaves:** A homologação será realizada rigorosamente no ambiente Sandbox da RV Hub usando dados controlados antes do *go-live*.

## Fora de Escopo

- Emissão de boletos ou cobrança Pix para aprovação avulsa (apenas cartão de crédito pré-cadastrado via tokenização).
- Aprovações automáticas baseadas em saldo: todas as emissões necessitarão de aprovação manual dos pais, sem exceção.
- Recargas de minutos/dados de operadoras de Celular e Pagamentos de Títulos/Cash-in. O escopo é estritamente limitado à "Recarga de PIN" (Gift Cards).
- Busca livre ou vitrine universal do RVHub sem curadoria.
