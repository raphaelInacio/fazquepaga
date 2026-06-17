# Tarefa 1.0: Camada de Cliente RV Hub (`RVHubClient`)

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Criar o cliente REST responsável por interagir com a API de "Recarga de PIN" do RVHub (ambiente Sandbox).

<skills>
### Conformidade com Skills Padrões

- `use-java-spring-boot.md`: Uso de WebClient/RestTemplate do Spring e padrões estruturais.
- `api-rest-http.md`: Contratos de requisição e formatação JSON.
</skills>

<requirements>
- Implementar chamada de autenticação JWT via Basic Auth.
- Implementar rotas `/pin-topups/transactions` (POST) para solicitar e capturar os PINs.
- O client deve ser capaz de receber um `X-Idempotency-Key` na solicitação.
</requirements>

## Subtarefas

- [ ] 1.1 Criar classes DTO para Requisição/Resposta do RVHub.
- [ ] 1.2 Implementar método de autenticação e cache do JWT.
- [ ] 1.3 Implementar envio de "Solicitação" passando a chave de idempotência.
- [ ] 1.4 Implementar rotina de "Captura".

## Detalhes de Implementação

A arquitetura e os contratos necessários estão detalhados na `techspec.md`, sob a seção `RVHubClient`.

## Critérios de Sucesso

- O client consegue simular chamadas e tratar falhas 4xx e 5xx vindas do Sandbox RVHub.
- O token JWT é armazenado em cache evitando logins a cada transação.

## Testes da Tarefa

- [ ] Testes de unidade (Mock/WireMock do serviço externo).
- [ ] Testes de integração.
- [ ] Testes E2E (se aplicável).

<critical>SEMPRE CRIE E EXECUTE OS TESTES DA TAREFA ANTES DE CONSIDERÁ-LA FINALIZADA</critical>

## Arquivos relevantes

- `backend/src/main/java/com/fazquepaga/taskandpay/giftcard/RVHubClient.java` (A Criar)
- `backend/src/main/java/com/fazquepaga/taskandpay/giftcard/dto/*`
