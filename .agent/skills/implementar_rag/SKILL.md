---
name: implementar_rag
description: Adiciona pipeline RAG (Retrieval-Augmented Generation) ao agente com client vector search, hybrid search e reordenação de contexto.
---

# Skill: Implementar Pipeline RAG

## Trigger
Quando o usuário pedir para adicionar RAG (Retrieval-Augmented Generation) ao agente.

## Pipeline Completo

```
Ingestão → Chunking → Embedding → Indexação → Retrieval → Montagem de Contexto → Geração
```

## Passos
1. Adicionar `RAGDocument` ao estado (`frozen=True`)
2. Implementar client de embedding + busca em `src/clients/vector_search.py`
3. Criar nó `src/nodes/rag_retrieval.py`
4. Conectar ao grafo como entry point
5. Adicionar config ao `.env.example`

## Template do Modelo RAGDocument (state.py)

```python
from pydantic import BaseModel, ConfigDict, Field
from typing import Any

class RAGDocument(BaseModel):
    """Documento recuperado do Vector Store. Frozen para evitar mutação."""
    model_config = ConfigDict(frozen=True)

    id: str
    distance: float = Field(ge=0.0)
    text: str
    metadata: dict[str, Any] = Field(default_factory=dict)

    def preview(self, max_chars: int = 240) -> str:
        cleaned = self.text.strip().replace("\n", " ")
        return cleaned if len(cleaned) <= max_chars else cleaned[:max_chars - 3] + "..."
```

## Template do Client Vector Search

```python
from google.cloud import aiplatform
from functools import lru_cache

@lru_cache(maxsize=1)
def _get_embedding_model():
    return TextEmbeddingModel.from_pretrained("text-embedding-004")

def embed_query(text: str) -> list[float]:
    model = _get_embedding_model()
    embeddings = model.get_embeddings([TextEmbeddingInput(text, "RETRIEVAL_QUERY")])
    return embeddings[0].values

def search(query_embedding: list[float], top_k: int = 5) -> list[RAGDocument]:
    endpoint = aiplatform.MatchingEngineIndexEndpoint(endpoint_name)
    response = endpoint.find_neighbors(
        deployed_index_id=deployed_id,
        queries=[query_embedding],
        num_neighbors=top_k,
    )
    return [RAGDocument(id=n.id, distance=n.distance, text=...) for n in response[0]]
```

## Template do Nó RAG

```python
def node_rag_retrieval(state: OrchestratorState) -> dict:
    logger.info("node_rag_retrieval | query=%s", state.question[:80])
    embedding = embed_query(state.question)
    documents = search(embedding, top_k=settings.vertex_rag_top_k)
    logger.info("node_rag_retrieval | retrieved %d docs", len(documents))
    return {"rag_context": documents}
```

## Estratégias de Chunking

| Estratégia | Quando Usar | Como |
|---|---|---|
| **Fixed com Overlap** | Prototipagem rápida | `RecursiveCharacterTextSplitter(chunk_size=500, overlap=50)` |
| **Semântico** | Docs com tópicos variados | Distância de cosseno entre sentenças adjacentes |
| **Parent-Child** | Precisão + contexto amplo | Busca por child pequeno, retorna parent grande |

## Busca Híbrida (Dense + Sparse)

Busca vetorial falha em termos exatos (IDs, CPFs, datas). A solução:

```
Query → Vector Search (semântico) ─┐
                                    ├→ RRF Fusion → Re-ranking → Top-K
Query → BM25 (keyword exato) ──────┘

RRF Score = 1/(k + rank_semantic) + 1/(k + rank_keyword)   # k=60
```

## Montagem de Contexto (Lost in the Middle)

LLMs ignoram informações no meio do prompt. Reordene chunks:
- **Mais relevantes** → início e fim do contexto
- **Menos relevantes** → meio

## Avaliação com RAGAS

| Métrica | O que mede |
|---|---|
| **Faithfulness** | Resposta deriva dos chunks? (sem alucinação) |
| **Answer Relevance** | Resposta atende à query? |
| **Context Precision** | Chunks recuperados são úteis ou ruído? |
| **Context Recall** | Todos os fatos necessários foram recuperados? |

## Config (.env.example)

```
VECTOR_SEARCH_INDEX_NAME=projects/<num>/locations/<loc>/indexes/<id>
VECTOR_SEARCH_INDEX_ENDPOINT_NAME=projects/<num>/locations/<loc>/indexEndpoints/<id>
VECTOR_SEARCH_DEPLOYED_INDEX_ID=<deployed_id>
VERTEX_EMBEDDING_MODEL=text-embedding-004
VERTEX_RAG_TOP_K=5
```

## Regras
- `RAGDocument` DEVE ser `frozen=True` (imutável no estado)
- Mesmo modelo de embedding na ingestão e na busca
- Para termos exatos, sempre considere busca híbrida
- Reordene chunks para evitar "Lost in the Middle"
