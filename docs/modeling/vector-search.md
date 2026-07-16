# AI Workspace — Estratégia de Busca Vetorial

**Versão:** 1.0  
**Data:** 2026-07-14  
**Status:** Aprovado

---

## Decisão: PostgreSQL + pgvector

Busca semântica via pgvector com índice HNSW. Zero infraestrutura adicional para o MVP.  
Migração para Qdrant possível sem alterar nenhum use case (troca de Adapter).

## Modelo de Embedding

- **Padrão MVP:** `text-embedding-3-small` (OpenAI) — 1536 dimensões
- **Abstração:** `EmbeddingPort` com `EmbeddingFactory` — plugável para Cohere, Voyage AI, HuggingFace, Ollama

## Estratégia de Chunking

- Tamanho: ~500 tokens com overlap de 50 tokens
- Estratégia: por parágrafo (preserva coerência semântica)
- Interface `ChunkingStrategy` plugável por formato (PDF, DOCX, Markdown, HTML)
- Metadata por chunk: página, posição, seção, idioma, tipo, versão, data de indexação

## Caminho de Evolução

MVP → pgvector (HNSW) → Qdrant (quando volume justificar extração)
