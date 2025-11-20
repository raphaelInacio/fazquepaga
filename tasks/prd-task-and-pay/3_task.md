---
status: completed
---

# Tarefa 3.0: Implementação do Módulo `allowance`

## Visão Geral

Esta tarefa consiste em desenvolver o motor de cálculo da mesada. A lógica determinará o valor monetário de cada tarefa com base em um valor total de mesada mensal e nos pesos atribuídos a cada tarefa. Este é um componente crítico da lógica de negócios do sistema.

**LEITURA OBRIGATÓRIA**: Antes de iniciar, revise as regras do projeto em `docs/ai_guidance/rules/`.

## Requisitos

-   Implementar a interface `AllowanceCalculator`.
-   A lógica deve calcular o "valor por ponto" com base na mesada mensal total e no somatório de pontos de todas as tarefas do mês.
-   O cálculo deve lidar dinamicamente com o número variável de dias em um mês para tarefas diárias e semanais.
-   O valor de cada tarefa individual deve ser calculado com base em seus pontos.

## Subtarefas

- [x] 3.1 Criar a classe de implementação para a interface `AllowanceCalculator`.
- [x] 3.2 Desenvolver a lógica para converter pesos de tarefas (Baixo, Médio, Alto) em pontos (ex: 1, 5, 20).
- [x] 3.3 Implementar o algoritmo para calcular o total de pontos possíveis em um determinado mês, considerando tarefas diárias, semanais e únicas.
- [x] 3.4 Implementar o cálculo do "valor por ponto" (mesada total / total de pontos).
- [x] 3.5 Expor um serviço que utilize o `AllowanceCalculator` para determinar o valor de uma tarefa concluída.
- [x] 3.6 Implementar testes unitários exaustivos para o `AllowanceCalculator`, cobrindo vários meses, tipos de tarefas e casos extremos (ex: mesada zero, nenhuma tarefa).

## Detalhes da Implementação

Esta tarefa é puramente de lógica de negócios e deve ter o mínimo de dependências externas. A complexidade reside em calcular corretamente o "total de pontos" para um mês. Por exemplo, uma tarefa diária vale (pontos * número de dias no mês), enquanto uma tarefa semanal vale (pontos * número de ocorrências daquele dia da semana no mês).

### Arquivos Relevantes

-   `allowance/AllowanceCalculator.java`
-   `allowance/AllowanceCalculatorImpl.java`
-   `allowance/AllowanceService.java`

## Critérios de Sucesso

-   O `AllowanceCalculator` calcula corretamente o valor de tarefas individuais com base nos exemplos fornecidos.
-   Os testes unitários cobrem todos os cenários descritos e atingem a meta de cobertura de 80%.
-   A lógica é bem documentada (se necessário) e fácil de entender.
-   O código é revisado e aprovado.
-   Todos os testes passam.
