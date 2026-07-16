-- Document Intelligence Schema

CREATE TABLE documents.documents (
    id                UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id          UUID         NOT NULL,
    title             VARCHAR(500) NOT NULL,
    storage_reference VARCHAR(1000) NOT NULL,
    mime_type         VARCHAR(100) NOT NULL,
    size_bytes        BIGINT       NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'UPLOADED'
                      CHECK (status IN ('UPLOADED', 'INDEXING', 'INDEXED', 'FAILED')),
    page_count        INTEGER,
    detected_language VARCHAR(10),
    document_type     VARCHAR(50),
    version           INTEGER      NOT NULL DEFAULT 1,
    indexed_at        TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Each chunk stores its full embedding provenance metadata.
-- This ensures we can identify and re-index chunks if the embedding model changes,
-- and supports future multi-model scenarios without schema changes.
CREATE TABLE documents.document_chunks (
    id                   UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_id          UUID        NOT NULL,
    content              TEXT        NOT NULL,
    chunk_index          INTEGER     NOT NULL,   -- zero-based sequential index used by the adapter
    chunk_order          INTEGER     NOT NULL,   -- display/sort order (mirrors chunk_index)
    start_offset         INTEGER,
    end_offset           INTEGER,
    token_count          INTEGER,
    page_number          INTEGER,
    section_title        VARCHAR(500),
    chunking_strategy    VARCHAR(50),
    detected_language    VARCHAR(10),
    document_type        VARCHAR(50),
    document_version     INTEGER     NOT NULL DEFAULT 1,
    embedding_provider   VARCHAR(50),            -- openai, cohere, voyage, huggingface, ollama
    embedding_model      VARCHAR(100),           -- text-embedding-3-small, embed-english-v3.0, etc.
    embedding_dimension  INTEGER,               -- 1536 for text-embedding-3-small
    embedding_generated_at TIMESTAMPTZ,
    pipeline_version     VARCHAR(20) NOT NULL DEFAULT '1',
    indexed_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    embedding            vector(1536),          -- pgvector column for semantic search
    CONSTRAINT fk_chunk_document FOREIGN KEY (document_id)
        REFERENCES documents.documents(id) ON DELETE CASCADE
);

CREATE TABLE documents.conversations (
    id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_id UUID        NOT NULL,
    user_id     UUID        NOT NULL,
    title       VARCHAR(500),
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_conversation_document FOREIGN KEY (document_id)
        REFERENCES documents.documents(id) ON DELETE CASCADE
);

CREATE TABLE documents.messages (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID        NOT NULL,
    role            VARCHAR(10) NOT NULL CHECK (role IN ('USER', 'ASSISTANT')),
    content         TEXT        NOT NULL,
    provider_used   VARCHAR(50),
    model_used      VARCHAR(100),
    tokens_used     INTEGER,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id)
        REFERENCES documents.conversations(id) ON DELETE CASCADE
);

-- Ingestion pipeline metrics for observability and cost optimization.
CREATE TABLE documents.ingestion_metrics (
    id                 UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_id        UUID          NOT NULL,
    processing_time_ms BIGINT,
    page_count         INTEGER,
    chunk_count        INTEGER,
    total_tokens       INTEGER,
    embedding_cost_usd DECIMAL(10,6),
    indexing_time_ms   BIGINT,
    embedding_provider VARCHAR(50),
    embedding_model    VARCHAR(100),
    pipeline_version   INTEGER,
    status             VARCHAR(20)   NOT NULL CHECK (status IN ('SUCCESS', 'FAILED', 'PARTIAL')),
    error_message      TEXT,
    started_at         TIMESTAMPTZ   NOT NULL,
    completed_at       TIMESTAMPTZ,
    CONSTRAINT fk_metrics_document FOREIGN KEY (document_id)
        REFERENCES documents.documents(id) ON DELETE CASCADE
);
