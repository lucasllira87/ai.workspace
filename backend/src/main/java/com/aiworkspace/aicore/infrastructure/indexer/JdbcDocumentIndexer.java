package com.aiworkspace.aicore.infrastructure.indexer;

import com.aiworkspace.aicore.application.port.out.DocumentIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Component
public class JdbcDocumentIndexer implements DocumentIndexer {

    private static final Logger log = LoggerFactory.getLogger(JdbcDocumentIndexer.class);

    private final JdbcTemplate jdbcTemplate;

    public JdbcDocumentIndexer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void markAsIndexed(UUID documentId, int chunkCount) {
        int updated = jdbcTemplate.update("""
                UPDATE documents.documents
                SET status = 'INDEXED',
                    chunk_count = ?,
                    indexed_at = ?,
                    error_message = NULL
                WHERE id = ?
                """, chunkCount, Timestamp.from(Instant.now()), documentId);

        if (updated == 0) {
            log.warn("Document {} not found when trying to mark as INDEXED", documentId);
        }
    }

    @Override
    public void markAsError(UUID documentId, String errorMessage) {
        int updated = jdbcTemplate.update("""
                UPDATE documents.documents
                SET status = 'FAILED',
                    error_message = ?
                WHERE id = ?
                """, errorMessage, documentId);

        if (updated == 0) {
            log.warn("Document {} not found when trying to mark as ERROR", documentId);
        }
    }
}
