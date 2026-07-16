package com.aiworkspace.aicore.infrastructure.vector;

import com.aiworkspace.aicore.application.port.out.VectorStore;
import com.aiworkspace.aicore.domain.model.Chunk;
import com.aiworkspace.aicore.domain.model.ChunkMetadata;
import com.aiworkspace.aicore.domain.model.EmbeddingVector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class PgVectorStoreAdapter implements VectorStore {

    private static final String PIPELINE_VERSION = "1";

    private final JdbcTemplate jdbcTemplate;

    public PgVectorStoreAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void storeChunks(List<ChunkWithEmbedding> chunks) {
        String sql = """
                INSERT INTO documents.document_chunks
                    (id, document_id, content, chunk_index, chunk_order, start_offset, end_offset,
                     token_count, page_number, section_title, chunking_strategy,
                     embedding_provider, embedding_model, embedding_dimension,
                     embedding_generated_at, pipeline_version, embedding)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::vector)
                ON CONFLICT (id) DO UPDATE SET
                    embedding = EXCLUDED.embedding,
                    embedding_generated_at = EXCLUDED.embedding_generated_at
                """;

        List<Object[]> batchArgs = new ArrayList<>();
        for (ChunkWithEmbedding cwe : chunks) {
            Chunk chunk = cwe.chunk();
            EmbeddingVector embedding = cwe.embedding();
            ChunkMetadata meta = chunk.metadata();

            batchArgs.add(new Object[]{
                    chunk.id(),
                    chunk.documentId(),
                    chunk.content(),
                    meta.chunkIndex(),
                    meta.chunkIndex(),   // chunk_order mirrors chunk_index (sequential position)
                    meta.startOffset(),
                    meta.endOffset(),
                    meta.estimatedTokenCount(),
                    meta.pageNumber(),
                    meta.sectionTitle(),
                    meta.chunkingStrategy(),
                    embedding.model().value().contains("openai") ? "openai"
                            : embedding.model().value().contains("google") ? "google" : "unknown",
                    embedding.model().value(),
                    embedding.dimensions(),
                    Timestamp.from(Instant.now()),
                    PIPELINE_VERSION,
                    embedding.toPgVectorString()
            });
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public List<ChunkSearchResult> similaritySearch(UUID documentId, EmbeddingVector query,
                                                      int limit, double threshold) {
        String vectorStr = query.toPgVectorString();
        String sql = """
                SELECT id, document_id, content, chunk_index, start_offset, end_offset,
                       token_count, page_number, section_title, chunking_strategy,
                       1 - (embedding <=> ?::vector) AS similarity
                FROM documents.document_chunks
                WHERE document_id = ?
                  AND embedding IS NOT NULL
                  AND 1 - (embedding <=> ?::vector) >= ?
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> toSearchResult(rs),
                vectorStr, documentId, vectorStr, threshold, vectorStr, limit);
    }

    @Override
    public void deleteByDocumentId(UUID documentId) {
        jdbcTemplate.update(
                "DELETE FROM documents.document_chunks WHERE document_id = ?", documentId);
    }

    private ChunkSearchResult toSearchResult(ResultSet rs) throws SQLException {
        ChunkMetadata metadata = ChunkMetadata.withPage(
                rs.getInt("chunk_index"),
                rs.getInt("start_offset"),
                rs.getInt("end_offset"),
                rs.getInt("token_count"),
                rs.getObject("page_number") != null ? rs.getInt("page_number") : null,
                rs.getString("section_title"),
                rs.getString("chunking_strategy"));

        Chunk chunk = new Chunk(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("document_id")),
                rs.getString("content"),
                metadata);

        double similarity = rs.getDouble("similarity");
        return new ChunkSearchResult(chunk, similarity);
    }
}
