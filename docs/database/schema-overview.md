# AI Workspace — Visão Geral do Schema de Banco de Dados

**Versão:** 1.1 (ajustes aprovados)  
**Data:** 2026-07-14

## Schemas por Bounded Context

| Schema | Contexto | Status |
|---|---|---|
| `iam` | Identity & Access | Implementado |
| `documents` | Document Intelligence | Implementado |
| `learning` | Learning Platform | Implementado |
| `aicore` | AI Core (cross-cutting) | Implementado |
| `audit` | Audit & Compliance | Implementado |
| `finance` | Finance Manager | Reservado (roadmap) |
| `code_review` | Code Reviewer | Reservado (roadmap) |

## Extensões PostgreSQL

- `uuid-ossp` — geração de UUIDs
- `vector` (pgvector) — busca semântica via embeddings

## Tabelas Principais

### iam
- `users` — aggregate root do usuário
- `user_profiles` — perfil e preferências
- `user_roles` — roles do usuário (tabela de junção)
- `refresh_tokens` — sessões ativas, com dispositivo, IP, last_used_at, revogação individual e global

### documents
- `documents` — documentos enviados
- `document_chunks` — chunks com embedding e metadados completos de provenance
- `conversations` — histórico de chat por documento
- `messages` — mensagens individuais com provider e model utilizados
- `ingestion_metrics` — métricas do pipeline de ingestão

### learning
- `study_materials` — materiais de estudo
- `summaries` — resumos gerados (1:1 com material)
- `questions` — questões de múltipla escolha (JSONB para opções)
- `flashcards` — cartões frente/verso

### aicore
- `prompt_templates` — prompts versionados e ativados por use case
- `provider_configs` — configuração por provedor e use case
- `usage_metrics` — custo, tokens, latência por requisição de IA

### audit
- `audit_events` — particionado por ano; append-only

## Índice HNSW (pgvector)

```sql
CREATE INDEX idx_chunks_embedding ON documents.document_chunks
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);
```

## Migrations (Flyway)

| Versão | Arquivo | Conteúdo |
|---|---|---|
| V001 | create_schemas | Criação dos schemas |
| V002 | enable_extensions | uuid-ossp e pgvector |
| V003 | iam_tables | IAM completo |
| V004 | aicore_tables | Prompts, configs, métricas de IA |
| V005 | documents_tables | Documents, chunks, conversas, ingestão |
| V006 | documents_embedding_column | Coluna vector(1536) |
| V007 | learning_tables | Materiais, resumos, questões, flashcards |
| V008 | audit_tables | Audit particionado por ano |
| V009 | indexes | Todos os índices incluindo HNSW |
