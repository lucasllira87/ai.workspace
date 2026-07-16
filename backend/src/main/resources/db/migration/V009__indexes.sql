-- Performance indexes

-- IAM
CREATE INDEX idx_refresh_tokens_user_id    ON iam.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_active     ON iam.refresh_tokens(user_id, expires_at)
    WHERE revoked_at IS NULL;              -- partial index: only active tokens

-- Documents
CREATE INDEX idx_documents_owner_id        ON documents.documents(owner_id);
CREATE INDEX idx_documents_status          ON documents.documents(status);
CREATE INDEX idx_chunks_document_id        ON documents.document_chunks(document_id);
CREATE INDEX idx_chunks_provider_model     ON documents.document_chunks(embedding_provider, embedding_model);
CREATE INDEX idx_conversations_user_id     ON documents.conversations(user_id);
CREATE INDEX idx_conversations_document_id ON documents.conversations(document_id);
CREATE INDEX idx_messages_conversation_id  ON documents.messages(conversation_id);
CREATE INDEX idx_ingestion_doc_id          ON documents.ingestion_metrics(document_id);

-- HNSW vector index for semantic search.
-- m=16, ef_construction=64: balanced precision/recall trade-off for MVP volume.
-- Built separately here to keep it visible as a distinct infrastructure concern.
-- For production with >1M vectors, consider increasing ef_construction to 128.
CREATE INDEX idx_chunks_embedding ON documents.document_chunks
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- Learning
CREATE INDEX idx_study_materials_owner ON learning.study_materials(owner_id);
CREATE INDEX idx_study_materials_status ON learning.study_materials(status);
CREATE INDEX idx_questions_material_id  ON learning.questions(material_id);
CREATE INDEX idx_flashcards_material_id ON learning.flashcards(material_id);

-- AI Core
CREATE INDEX idx_usage_metrics_user_id    ON aicore.usage_metrics(user_id);
CREATE INDEX idx_usage_metrics_occurred   ON aicore.usage_metrics(occurred_at DESC);
CREATE INDEX idx_usage_metrics_resource   ON aicore.usage_metrics(resource_type, resource_id);

-- Audit indexes are created in V015 alongside the definitive audit_events schema.
