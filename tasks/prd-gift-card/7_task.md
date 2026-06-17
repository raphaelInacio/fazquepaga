# Tarefa 7.0: Testes End-to-End (Playwright)

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Garantir o fluxo de valor ponta-a-ponta, desde a solicitação da criança até a aprovação financeira do pai, validando a integração Frontend e Backend (este último mockando a resposta da RVHub na Sandbox).

<skills>
### Conformidade com Skills Padrões

- `e2e-testing.md`: Escrever os testes E2E com Playwright seguindo a convenção da suíte principal.
</skills>

<requirements>
- Criar o cenário de login do Dependente, inclusão de Gift Card e criação do Request.
- Criar o cenário trocando a sessão (ou rodando logo a seguir) no Dashboard do Pai, aprovando o Request e validando a notificação de sucesso.
- Validar as UI constraints (Aparecimento de avisos, botões, modais).
</requirements>

## Subtarefas

- [ ] 7.1 Setup do mock E2E para interceptar rotas de pagamento / RVHub (se necessário no e2e).
- [ ] 7.2 Implementar fluxo do filho.
- [ ] 7.3 Implementar fluxo do pai aprovando.
- [ ] 7.4 Testar caso negativo: Pai aprova, mas API retorna falha (validar state do UI).

## Detalhes de Implementação

Seguir o `e2e-testing.md`. O teste deve rodar sem flaky no CI.

## Critérios de Sucesso

- Testes passando consistentemente na suíte E2E do frontend via comando `npm run test:e2e`.

## Testes da Tarefa

- [ ] Testes de unidade
- [ ] Testes de integração
- [x] Testes E2E (Foco da tarefa)

<critical>SEMPRE CRIE E EXECUTE OS TESTES DA TAREFA ANTES DE CONSIDERÁ-LA FINALIZADA</critical>

## Arquivos relevantes

- `frontend/tests/e2e/giftcard-integration.spec.ts`
