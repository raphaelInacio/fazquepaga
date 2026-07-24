# Relatório de QA - Integração de Gift Cards via RV Hub

## Resumo
- Data: 17/06/2026
- Status: REPROVADO
- Total de Requisitos: 6
- Requisitos Atendidos: 1 (Visualização Parcial)
- Bugs Encontrados: 6

## Requisitos Verificados
| ID | Requisito | Status | Evidência |
|----|-----------|--------|-----------|
| RF-01 | Dependente visualiza catálogo de Gift Cards | PARCIAL | Catalog visível após bypass manual de Premium |
| RF-02 | Dependente seleciona Gift Card e envia solicitação | FALHOU | Bloqueado por saldo 0 e falha no Firestore |
| RF-03 | Status do pedido fica "Aguardando Aprovação" | NÃO TESTADO | - |
| RF-04 | Responsável visualiza pedidos pendentes | NÃO TESTADO | - |
| RF-05 | Responsável aprova pedido (com aviso de cobrança real) | NÃO TESTADO | - |
| RF-06 | Dependente visualiza código PIN após aprovação | NÃO TESTADO | - |

## Testes E2E Executados
| Fluxo | Resultado | Observações |
|-------|-----------|-------------|
| Onboarding de Responsável e Dependente | PASSOU | Cadastro e criação de código de integração funcionaram |
| Login de Dependente via Código | FALHOU | Exigiu correção de código no frontend para persistir sessão |
| Navegação para Loja de Gift Cards | FALHOU | Bloqueado por redirecionamentos de 401 e falta de role PREMIUM |
| Fluxo de Compra Completo | FALHOU | Impedido por permissões de escrita no Firestore GCP |

## Acessibilidade
- [x] Navegação por teclado básica (Tab/Enter) funcional nos formulários.
- [ ] Diversos elementos interativos sem labels de tradução (exibindo chaves raw).
- [x] Contrastes visuais na loja de Gift Cards seguem bons padrões de design.

## Bugs Encontrados (Resumo)
| ID | Descrição | Severidade | Screenshot |
|----|-----------|------------|------------|
| BUG-01 | Loop de Redirecionamento no Login de Dependentes | Alta (Bloqueante) | `tasks/prd-gift-card/bugs.md` |
| BUG-02 | Incompatibilidade de LocalStorage | Média | `tasks/prd-gift-card/bugs.md` |
| BUG-03 | Acesso Negado ao carregar dados do Dependente (401) | Alta | `tasks/prd-gift-card/bugs.md` |
| BUG-04 | Falha de Permissão no Firestore GCP | Crítica (Infra) | `tasks/prd-gift-card/bugs.md` |
| BUG-05 | Chaves de tradução ausentes | Baixa | `tasks/prd-gift-card/bugs.md` |

## Conclusão
A funcionalidade de Gift Card está tecnicamente implementada conforme a Tech Spec, porém é impossível de ser utilizada no ambiente atual sem as correções de infraestrutura (Firestore) e os ajustes finos de autenticação no frontend realizados durante este QA. A experiência do usuário dependente é severamente impactada pelo loop de login. O status é REPROVADO até que os bugs críticos e os problemas de permissão na nuvem sejam sanados.
