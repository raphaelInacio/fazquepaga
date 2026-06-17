# Tarefa 5.0: Frontend - Interface do Dependente (Vitrine e Carrinho)

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Construir a vitrine de Gift Cards voltada para as crianças/dependentes em React, garantindo uma experiência visual e a capacidade de solicitar aprovação de compras.

<skills>
### Conformidade com Skills Padrões

- `react.md`: Uso de componentes funcionais, hooks e separação de lógica.
- `frontend-design`: Implementar um design moderno, visual (categorias) para os Gift Cards (Roblox, PlayStation, Uber, iFood).
- `shadcn`: Reutilização de componentes base do UI.
</skills>

<requirements>
- Listar produtos organizados em seções (ex: Jogos, Delivery).
- Componente de Carrinho/Pedido para que o dependente clique em "Pedir ao Responsável".
- Feedback em tela ("Enviado para aprovação").
</requirements>

## Subtarefas

- [ ] 5.1 Criar componente da página de catálogo.
- [ ] 5.2 Implementar listagem mockada enquanto backend sobe ou plugar os serviços de API existentes no frontend.
- [ ] 5.3 Criar o botão e lógica de "Solicitar", consumindo a API de Requests.
- [ ] 5.4 Testes de UI.

## Detalhes de Implementação

Seguir UX da seção "Experiência do Usuário" do `prd.md`. O frontend consome os serviços expostos no backend na Tarefa 4.

## Critérios de Sucesso

- Uma UI atraente para crianças e que responde rapidamente à ação de adicionar ao carrinho.

## Testes da Tarefa

- [ ] Testes de unidade/componentes (Jest/RTL)
- [ ] Testes de integração
- [ ] Testes E2E (será coberto na Tarefa 7)

<critical>SEMPRE CRIE E EXECUTE OS TESTES DA TAREFA ANTES DE CONSIDERÁ-LA FINALIZADA</critical>

## Arquivos relevantes

- `frontend/src/pages/GiftCards/`
- `frontend/src/services/GiftCardService.ts`
