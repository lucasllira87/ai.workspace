-- Add vector embedding column to document_chunks.
-- Separated from table creation to make the vector infrastructure dependency explicit.
-- MVP: 1536 dimensions (text-embedding-3-small / OpenAI).
-- Future: this column will be migrated if a different dimension model is adopted.
-- See ADR-008 for embedding provider strategy.

ALTER TABLE documents.document_chunks
    ADD COLUMN embedding vector(1536);
