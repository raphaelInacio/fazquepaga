---
name: criar_no_langgraph
description: Cria um novo nó no grafo do agente LangGraph incluindo código, state, enums, testes unitários e roteamento.
---

# Skill: Criar Nó LangGraph

## Trigger
Quando o usuário pedir para criar um novo nó no grafo do agente.

## Passos
1. Criar arquivo em `src/nodes/{nome_do_no}.py`
2. Implementar função com assinatura: `def node_{nome}(state: OrchestratorState) -> dict:`
3. Documentar campos lidos e escritos na docstring
4. Adicionar campos necessários ao `OrchestratorState` em `src/state.py` (COM defaults seguros)
5. Registrar o nó no `StrEnum Node` em `src/constants.py`
6. Adicionar `g.add_node(Node.{NOME}, node_{nome})` em `src/graph.py`
7. Conectar com `add_edge` ou `add_conditional_edges`
8. Re-exportar em `src/nodes/__init__.py`
9. Criar teste em `tests/unit/test_{nome}.py`

## Template do Nó

```python
"""Nó {nome}: {responsabilidade em uma frase}."""

from __future__ import annotations

from ..logging_config import get_logger
from ..state import OrchestratorState

logger = get_logger(__name__)


def node_{nome}(state: OrchestratorState) -> dict:
    """Descrição do que o nó faz.

    Lê:  state.campo_a, state.campo_b
    Escreve: campo_atualizado
    """
    logger.info("node_{nome} | processing...")
    # ... lógica ...
    return {"campo_atualizado": valor}
```

## Template do Enum (adicionar em constants.py)

```python
class Node(StrEnum):
    # ... nós existentes ...
    {NOME_UPPER} = "{nome_lower}"
```

## Template do Teste

```python
from src.state import OrchestratorState
from src.nodes.{nome} import node_{nome}

def test_node_{nome}_returns_expected_keys():
    state = OrchestratorState(question="pergunta de teste")
    result = node_{nome}(state)
    assert isinstance(result, dict)
    assert "campo_atualizado" in result
```

## Se o nó toma decisões (bifurcação condicional)

Use `with_structured_output` para forçar o LLM a retornar JSON validado:

```python
from pydantic import BaseModel, Field

class Decision(BaseModel):
    requires_action: bool = Field(..., description="Critério da decisão")
    rationale: str = Field(..., min_length=5)

chain = prompt | llm.with_structured_output(Decision)
result: Decision = chain.invoke({...})
# result.requires_action é bool GARANTIDO pelo schema
```

Em seguida crie uma **função pura de roteamento** (sem IO):

```python
def route_after_{nome}(state: OrchestratorState) -> Node:
    return Node.PROXIMO_A if state.campo_decisao else Node.PROXIMO_B
```

## Se o nó chama serviço externo (graceful degradation)

```python
try:
    resultado = servico_externo(payload)
except ExternalServiceError as exc:
    logger.error("node_{nome} | service failed: %s", exc)
    resultado = "Serviço indisponível. Análise pendente."
return {"campo": resultado}
```

## Regras
- O nó NUNCA modifica o estado diretamente — retorna dict parcial
- Logger DEVE incluir nome do nó no prefixo
- Campos novos no estado DEVEM ter default seguro
- Nome no enum: UPPER_SNAKE_CASE. Valor: lower_snake_case
