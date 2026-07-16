-- V011: Expand documents schema for documents module

-- =====================================================
-- documents.documents: add error_message column
-- =====================================================
ALTER TABLE documents.documents
    ADD COLUMN IF NOT EXISTS error_message TEXT;

-- =====================================================
-- documents.document_chunks: add columns required by
-- PgVectorStoreAdapter (chunk_index, offsets, strategy)
-- =====================================================
ALTER TABLE documents.document_chunks
    ADD COLUMN IF NOT EXISTS chunk_index       INT,
    ADD COLUMN IF NOT EXISTS start_offset      INT,
    ADD COLUMN IF NOT EXISTS end_offset        INT,
    ADD COLUMN IF NOT EXISTS chunking_strategy VARCHAR(100);

-- Migrate existing chunk_order data to chunk_index for continuity
UPDATE documents.document_chunks
    SET chunk_index = chunk_order
    WHERE chunk_index IS NULL;

-- =====================================================
-- Index on documents status (owner + status for dashboard queries)
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_documents_owner_status
    ON documents.documents (owner_id, status);
