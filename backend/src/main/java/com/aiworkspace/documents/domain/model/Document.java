package com.aiworkspace.documents.domain.model;

import com.aiworkspace.documents.domain.event.DocumentDeletedEvent;
import com.aiworkspace.documents.domain.event.DocumentIndexedEvent;
import com.aiworkspace.documents.domain.event.DocumentUploadedEvent;
import com.aiworkspace.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Document extends AggregateRoot {

    private final UUID id;
    private final UUID ownerId;
    private final String title;
    private DocumentMetadata metadata;
    private DocumentStatus status;
    private String errorMessage;
    private Integer chunkCount;
    private Instant indexedAt;
    private final Instant uploadedAt;
    private Instant updatedAt;

    private Document(UUID id, UUID ownerId, String title, DocumentMetadata metadata,
                     DocumentStatus status, String errorMessage, Integer chunkCount,
                     Instant indexedAt, Instant uploadedAt, Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.metadata = metadata;
        this.status = status;
        this.errorMessage = errorMessage;
        this.chunkCount = chunkCount;
        this.indexedAt = indexedAt;
        this.uploadedAt = uploadedAt;
        this.updatedAt = updatedAt;
    }

    public static Document upload(UUID id, UUID ownerId, String title, DocumentMetadata metadata) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(metadata, "metadata");

        Instant now = Instant.now();
        Document doc = new Document(id, ownerId, title, metadata,
                DocumentStatus.UPLOADED, null, null, null, now, now);

        doc.registerEvent(new DocumentUploadedEvent(id, ownerId, title,
                metadata.mimeType(), metadata.storagePath(), now));
        return doc;
    }

    public static Document reconstitute(UUID id, UUID ownerId, String title,
                                         DocumentMetadata metadata, DocumentStatus status,
                                         String errorMessage, Integer chunkCount,
                                         Instant indexedAt, Instant uploadedAt, Instant updatedAt) {
        return new Document(id, ownerId, title, metadata, status,
                errorMessage, chunkCount, indexedAt, uploadedAt, updatedAt);
    }

    public void attachStoragePath(String storagePath) {
        this.metadata = metadata.withStoragePath(storagePath);
        this.updatedAt = Instant.now();
    }

    public void startIndexing() {
        this.status = DocumentStatus.INDEXING;
        this.updatedAt = Instant.now();
    }

    public void markAsIndexed(int chunkCount) {
        this.status = DocumentStatus.INDEXED;
        this.chunkCount = chunkCount;
        this.indexedAt = Instant.now();
        this.updatedAt = Instant.now();
        registerEvent(new DocumentIndexedEvent(id, ownerId, title, chunkCount, indexedAt));
    }

    public void markAsFailed(String errorMessage) {
        this.status = DocumentStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = Instant.now();
    }

    public void delete() {
        registerEvent(new DocumentDeletedEvent(id, ownerId, metadata.storagePath(), Instant.now()));
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public String getTitle() { return title; }
    public DocumentMetadata getMetadata() { return metadata; }
    public DocumentStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public Integer getChunkCount() { return chunkCount; }
    public Instant getIndexedAt() { return indexedAt; }
    public Instant getUploadedAt() { return uploadedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
