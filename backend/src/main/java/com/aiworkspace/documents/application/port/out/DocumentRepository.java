package com.aiworkspace.documents.application.port.out;

import com.aiworkspace.documents.domain.model.Document;
import com.aiworkspace.documents.domain.model.DocumentStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository {
    Document save(Document document);
    Optional<Document> findByIdAndOwnerId(UUID id, UUID ownerId);
    List<Document> findAllByOwnerId(UUID ownerId);
    List<Document> findStuckDocuments(DocumentStatus status, Instant uploadedBefore);
    void updateStoragePath(UUID documentId, String storagePath);
    void updateStatus(UUID documentId, DocumentStatus status, String errorMessage);
    void delete(UUID documentId);
    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);
}
