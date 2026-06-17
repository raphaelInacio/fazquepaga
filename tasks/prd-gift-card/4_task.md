# Tarefa 4.0: Exposição via API (`GiftCardController`)

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Substituir o controller que atualmente serve mock-data (`GiftCardController`) para se comunicar de fato com as lógicas do `GiftCardService`.

<skills>
### Conformidade com Skills Padrões

- `api-rest-http.md`: Validação de DTOs, Rest Controllers, HTTP Status corretos (201, 200).
- `use-java-spring-boot.md`: Uso do `@RestController` e `@Valid`.
</skills>

<requirements>
- Ajustar endpoint GET `/catalog` para retornar as marcas aprovadas (curadoria).
- Criar endpoint POST `/requests` para dependente iniciar a compra.
- Criar endpoint POST `/requests/{id}/approve` para pai concluir a compra.
</requirements>

## Subtarefas

- [ ] 4.1 Modificar o catálogo (`GET`).
- [ ] 4.2 Adicionar a rota de criação de requests.
- [ ] 4.3 Adicionar a rota de aprovação.
- [ ] 4.4 Integrar com a segurança (verificação de JWT/Papeis do usuário via Header).

## Detalhes de Implementação

Consulte os "Endpoints de API" listados no `techspec.md`.

## Critérios de Sucesso

- O Frontend consegue consumir as APIs sem quebrar, com formatação JSON coerente e tratamentos de erro unificados da aplicação (ex: Retornando 400 Bad Request se pai não tiver saldo no Asaas).

## Testes da Tarefa

- [ ] Testes de unidade
- [ ] Testes de integração (usando `MockMvc`)
- [ ] Testes E2E (se aplicável)

<critical>SEMPRE CRIE E EXECUTE OS TESTES DA TAREFA ANTES DE CONSIDERÁ-LA FINALIZADA</critical>

## Arquivos relevantes

- `backend/src/main/java/com/fazquepaga/taskandpay/giftcard/GiftCardController.java`
