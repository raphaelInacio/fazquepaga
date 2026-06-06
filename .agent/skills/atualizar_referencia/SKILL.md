---
name: atualizar_referencia
description: Atualiza o AGENTS.md e os arquivos de configuração (rules, skills, agents) com novos padrões identificados no projeto atual.
---

# Skill: Atualizar Guia de Referência

## Trigger
Quando o usuário pedir para atualizar o guia de boas práticas ou o AGENTS.md.

## Passos
1. Ler o `AGENTS.md` na raiz do workspace
2. Escanear o workspace buscando:
   - Novos diretórios de projeto não listados
   - Novos padrões de código (novos nós, integrações, clients)
   - Novas configurações YAML, templates Jinja2 ou dependências
3. Comparar com o guia existente e identificar gaps
4. Propor atualizações em formato de diff para o usuário aprovar
5. Atualizar a data de "Última atualização"

## Regras
- NÃO remove conteúdo existente, apenas adiciona ou atualiza
- Mantém a estrutura de seções existente
- SEMPRE pede aprovação do usuário antes de aplicar mudanças
- Se novos padrões são encontrados, atualiza também as skills e agents relevantes
