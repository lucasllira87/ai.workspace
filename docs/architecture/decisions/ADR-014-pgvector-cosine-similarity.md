# ADR-014: pgvector para Vector Store com Cosine Similarity

**Status:** Aceito  
**Data:** 2026-07-14  
**Módulo:** ai-core

---

## Contexto

O módulo de ingestão de documentos precisa armazenar embeddings e realizar buscas de similaridade semântica. A escolha do vector store afeta performance, operação, custo e complexidade arquitetural.

O projeto já usa PostgreSQL 16 como banco principal.

## Decisão

Usar **pgvector** (extensão do PostgreSQL) como vector store, via `JdbcTemplate` direto (sem ORM).

**Justificativas:**
1. Zero infraestrutura adicional — PostgreSQL já existe no `docker-compose.yml`
2. ACID garantido — chunks e metadados na mesma transação
3. SQL familiar — equipe não precisa aprender nova query language
4. `<=>` operator do pgvector é cosine distance nativa: `1 - (embedding <=> ?::vector)` = cosine similarity
5. `pgvector 0.1.6` já está no `pom.xml`

**Implementação:** `PgVectorStoreAdapter` usa `JdbcTemplate.batchUpdate()` para inserções (contorna limitações do JPA com arrays customizados) e SQL nativo para similaridade:

```sql
1 - (embedding <=> ?::vector) AS similarity
ORDER BY embedding <=> ?::vector
LIMIT ?
```

**Pendência crítica (DT-1):** Índice HNSW deve ser criado em V011:
```sql
CREATE INDEX idx_document_chunks_embedding_hnsw
    ON documents.document_chunks
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);
```
Sem HNSW, buscas vetoriais fazem sequential scan — inaceitável com >10k chunks.

## Alternativas descartadas

**A: Pinecone (managed vector DB):**  
Descartado — custo adicional, dependência externa, latência de rede, sem ACID com dados relacionais.

**B: Weaviate (self-hosted):**  
Descartado — nova infraestrutura Docker, curva de aprendizado, operação adicional.

**C: Qdrant:**  
Descartado — mesmo argumento de infraestrutura. Superior em performance pura mas desnecessário para MVP.

**D: Spring Data com `@Column(columnDefinition = "vector")` via JPA:**  
Descartado — JPA não tem suporte nativo para pgvector; workaround com `@Formula` cria SQL não-padrão e difícil de manter.

## Trade-offs

| Vantagem | Desvantagem |
|---|---|
| Zero infra adicional | Não escala horizontalmente como Pinecone |
| ACID com dados relacionais | Sem reranking nativo (precisa pós-processar) |
| SQL padrão | HNSW rebuild é pesado (mas raro) |
| `VectorStore` port permite migração futura | pgvector ocupa storage de 1536 × 4 bytes por embedding |

## Migração futura

Se escala exigir vector DB dedicado, o `VectorStore` port garante que a migração é de infraestrutura apenas:
```java
// Substituir PgVectorStoreAdapter por:
class PineconeVectorStoreAdapter implements VectorStore { ... }
```
Zero mudanças em application ou domain.

## Parâmetros de dimensão

- `text-embedding-3-small`: 1536 dimensões → 6KB por embedding
- 1M de chunks → ~6GB apenas em embeddings
- PostgreSQL suporta até 16MB por coluna → safe até embeddings de 4M dimensões
