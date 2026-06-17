# Tarefa 2.0: Persistência no Firestore (`GiftCardTransaction`)

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Estruturar o armazenamento das compras de Gift Cards no Firestore usando uma coleção raiz, permitindo o rastreamento dos status (Pendente, Aprovado, etc) e garantindo segurança transacional.

<skills>
### Conformidade com Skills Padrões

- `firestore-nosql.md`: Uso de coleção raiz, paginação e regras de acesso ao banco.
- `use-java-spring-boot.md`: Uso do Google Cloud Firestore via Spring.
</skills>

<requirements>
- Criar a entidade e repositório `GiftCardTransaction`.
- Persistir as chaves de integração externas (Asaas ID, RVHub ID, Idempotency Key).
- Gerenciar corretamente o UUID da requisição para controle de idempotência.
</requirements>

## Subtarefas

- [ ] 2.1 Criar a entidade modelo `GiftCardTransaction` com atributos listados na spec.
- [ ] 2.2 Criar `GiftCardTransactionRepository` com métodos de busca por `parentId` e `childId`.
- [ ] 2.3 Implementar testes de persistência.

## Detalhes de Implementação

Ver seção "Modelos de Dados" em `techspec.md` para a lista exata de propriedades do documento Firestore.

## Critérios de Sucesso

- Entidades persistidas com Timestamps e tipagens corretas (BigDecimal para valor financeiro, Enum para status).
- Consultas (Queries) operando de forma otimizada para listar transações de dependentes e pais.

## Testes da Tarefa

- [ ] Testes de unidade
- [ ] Testes de integração (Firestore Emulator/Testcontainers)
- [ ] Testes E2E (se aplicável)

<critical>SEMPRE CRIE E EXECUTE OS TESTES DA TAREFA ANTES DE CONSIDERÁ-LA FINALIZADA</critical>

## Arquivos relevantes

- `backend/src/main/java/com/fazquepaga/taskandpay/giftcard/GiftCardTransaction.java`
- `backend/src/main/java/com/fazquepaga/taskandpay/giftcard/GiftCardTransactionRepository.java`
