# Tarefa 6.0: Frontend - Dashboard dos Pais (Aprovação)

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Desenvolver o painel onde o pai recebe as requisições de Gift Cards, visualiza o aviso de cobrança (Warning) e pode Aprovar a transação.

<skills>
### Conformidade com Skills Padrões

- `react.md`: Utilização do Contexto/Custom Hooks para lidar com as chamadas autenticadas de pais.
- `shadcn`: Uso de Dialog, Alerts, e botões padrão.
</skills>

<requirements>
- Exibir os pedidos em status `PENDING` na Home do responsável ou aba específica de Aprovações.
- Ao clicar em aprovar, exibir o "Warning" explícito sobre a cobrança no cartão cadastrado, com valores.
- Processar o clique e consumir o endpoint `POST /requests/{id}/approve` do backend.
- Feedback de sucesso (ou erro, ex: "Cartão Recusado").
- Visualização do Voucher/PIN na tela do dependente (após o pai ter aprovado).
</requirements>

## Subtarefas

- [ ] 6.1 Listar pendências de aprovação de Gift Card.
- [ ] 6.2 Desenvolver a Dialog de Aviso e Aprovação (UI).
- [ ] 6.3 Conectar com o serviço de API.
- [ ] 6.4 Fazer a tela de Histórico do filho exibir o PIN gerado após sucesso.

## Detalhes de Implementação

Detalhes da experiência do usuário estão no `prd.md`, seção "Experiência do Usuário".

## Critérios de Sucesso

- O Pai compreende de forma absolutamente clara que a aprovação resulta em débito financeiro imediato via Asaas.
- Mensagens de erro da integração (como saldo insuficiente) são tratadas no frontend de forma amigável.

## Testes da Tarefa

- [ ] Testes de unidade/componentes (RTL)
- [ ] Testes de integração
- [ ] Testes E2E (será coberto na Tarefa 7)

<critical>SEMPRE CRIE E EXECUTE OS TESTES DA TAREFA ANTES DE CONSIDERÁ-LA FINALIZADA</critical>

## Arquivos relevantes

- `frontend/src/pages/ParentDashboard/`
- `frontend/src/components/GiftCardApprovalDialog.tsx`
