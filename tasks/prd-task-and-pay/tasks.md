# Resumo de Tarefas de Implementação — Clean-up do MVP (TaskAndPay)

> **Contexto**: Remoção das features não finalizadas (WhatsApp, Gift Card Store, Prova de Foto) do código e da UI para enxugar o MVP e alinhar o produto com o escopo real entregue.
>
> **Referências**: [PRD](./_prd.md) | [Tech Spec](./techspec.md)

## Tarefas

- [ ] 1.0 Backend: Remover NotificationService e infraestrutura de Pub/Sub
- [ ] 2.0 Backend: Desabilitar WhatsApp e neutralizar requiresProof
- [ ] 3.0 Frontend: Remover Gift Card Store (rotas, componentes, estados)
- [ ] 4.0 Frontend: Remover requiresProof e PENDING_APPROVAL da UI
- [ ] 5.0 Frontend: Remover menções a WhatsApp e Gift Card do conteúdo de marketing e Pricing
- [ ] 6.0 Verificação final: testes E2E e validação do fluxo completo do MVP
