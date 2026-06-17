# Tarefa 3.0: Orquestração e Regra de Negócio (`GiftCardService`)

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Implementar o serviço de negócio central da emissão de vouchers, coordenando as aprovações, a cobrança no gateway (`AsaasService`), o consumo da API parceira (`RVHubClient`) e a consolidação no banco (`GiftCardTransactionRepository`).

<skills>
### Conformidade com Skills Padrões

- `use-java-spring-boot.md`: Padrões de Service, Injeção de Dependências e transações.
- `asaas-integration.md`: Boas práticas para lidar com cobranças síncronas/estorno.
</skills>

<requirements>
- Lógica de Solicitação (Criança) que apenas cria o registro como `PENDING`.
- Lógica de Aprovação (Pai) que executa a cobrança no Asaas.
- Em caso de sucesso de cobrança, enviar solicitação e captura para a RVHub.
- Mecanismo de fallback: se RVHub der exceção/falha no meio, acionar `AsaasService` para estornar a cobrança do cartão.
</requirements>

## Subtarefas

- [ ] 3.1 Implementar método `requestGiftCard`.
- [ ] 3.2 Implementar método síncrono `approveGiftCard` com try-catch.
- [ ] 3.3 Escrever testes unitários validando estorno do Asaas em caso de falha da RV Hub.

## Detalhes de Implementação

Seguir o fluxograma e definições da `techspec.md`, seções `Orquestração` e `Riscos Conhecidos`.

## Critérios de Sucesso

- O saldo e o pagamento são perfeitamente consistentes; não há possibilidade de pai ser cobrado sem que a RVHub confirme o PIN, nem a RVHub emitir o PIN sem que o Asaas tenha autorizado.

## Testes da Tarefa

- [ ] Testes de unidade (Mockito para os serviços externos)
- [ ] Testes de integração
- [ ] Testes E2E (se aplicável)

<critical>SEMPRE CRIE E EXECUTE OS TESTES DA TAREFA ANTES DE CONSIDERÁ-LA FINALIZADA</critical>

## Arquivos relevantes

- `backend/src/main/java/com/fazquepaga/taskandpay/giftcard/GiftCardService.java`
