package com.aiworkspace.documents.infrastructure.persistence.adapter;

import com.aiworkspace.documents.application.port.out.DocumentRepository;
import com.aiworkspace.documents.domain.model.Document;
import com.aiworkspace.documents.domain.model.DocumentMetadata;
import com.aiworkspace.documents.domain.model.DocumentStatus;
import com.aiworkspace.documents.infrastructure.persistence.entity.DocumentJpaEntity;
import com.aiworkspace.documents.infrastructure.persistence.repository.DocumentJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Component
public class DocumentRepositoryAdapter implements DocumentRepository {

    private final DocumentJpaRepository jpaRepository;

    public DocumentRepositoryAdapter(DocumentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Document save(Document document) {
        DocumentJpaEntity entity = toEntity(document);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Document> findByIdAndOwnerId(UUID id, UUID ownerId) {
        return jpaRepository.findByIdAndOwnerId(id, ownerId).map(this::toDomain);
    }

    @Override
    public List<Document> findAllByOwnerId(UUID ownerId) {
        return jpaRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Document> findStuckDocuments(DocumentStatus status, Instant uploadedBefore) {
        return jpaRepository.findByStatusAndCreatedAtBefore(status.name(), uploadedBefore)
                .stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void updateStoragePath(UUID documentId, String storagePath) {
        jpaRepository.updateStoragePath(documentId, storagePath);
    }

    @Override
    @Transactional
    public void updateStatus(UUID documentId, DocumentStatus status, String errorMessage) {
        jpaRepository.updateStatus(documentId, status.name(), errorMessage);
    }

    @Override
    public void delete(UUID documentId) {
        jpaRepository.deleteById(documentId);
    }

    @Override
    public boolean existsByIdAndOwnerId(UUID id, UUID ownerId) {
        return jpaRepository.existsByIdAndOwnerId(id, ownerId);
    }

    private Document toDomain(DocumentJpaEntity e) {
        DocumentMetadata metadata = new DocumentMetadata(
                e.getMimeType(), e.getSizeBytes(), e.getStorageReference());

        return Document.reconstitute(
                e.getId(), e.getOwnerId(), e.getTitle(),
                metadata,
                DocumentStatus.valueOf(e.getStatus()),
                e.getErrorMessage(),
                e.getChunkCount(),
                e.getIndexedAt(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }

    private DocumentJpaEntity toEntity(Document doc) {
        DocumentJpaEntity entity = new DocumentJpaEntity();
        entity.setId(doc.getId());
        entity.setOwnerId(doc.getOwnerId());
        entity.setTitle(doc.getTitle());
        entity.setStorageReference(doc.getMetadata().storagePath());
        entity.setMimeType(doc.getMetadata().mimeType());
        entity.setSizeBytes(doc.getMetadata().sizeBytes());
        entity.setStatus(doc.getStatus().name());
        entity.setChunkCount(doc.getChunkCount());
        entity.setErrorMessage(doc.getErrorMessage());
        entity.setIndexedAt(doc.getIndexedAt());

        Instant now = Instant.now();
        entity.setCreatedAt(doc.getUploadedAt() != null ? doc.getUploadedAt() : now);
        entity.setUpdatedAt(doc.getUpdatedAt() != null ? doc.getUpdatedAt() : now);
        return entity;
    }
}
