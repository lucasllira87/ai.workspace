package com.aiworkspace.audit.infrastructure.stats;

import com.aiworkspace.audit.application.port.out.DocumentStatsPort;
import com.aiworkspace.audit.domain.model.DocumentStats;
import com.aiworkspace.documents.infrastructure.persistence.repository.DocumentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DocumentStatsAdapter implements DocumentStatsPort {

    private final DocumentJpaRepository documentJpaRepository;

    public DocumentStatsAdapter(DocumentJpaRepository documentJpaRepository) {
        this.documentJpaRepository = documentJpaRepository;
    }

    @Override
    public DocumentStats getStatsForUser(UUID userId) {
        long total = documentJpaRepository.countByOwnerId(userId);
        long indexed = documentJpaRepository.countByOwnerIdAndStatus(userId, "INDEXED");
        long storageBytes = documentJpaRepository.sumSizeBytesByOwnerId(userId);
        return new DocumentStats(total, indexed, storageBytes);
    }
}
