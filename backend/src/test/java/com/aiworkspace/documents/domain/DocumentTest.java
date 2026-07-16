package com.aiworkspace.documents.domain;

import com.aiworkspace.documents.domain.event.DocumentIndexedEvent;
import com.aiworkspace.documents.domain.event.DocumentUploadedEvent;
import com.aiworkspace.documents.domain.model.Document;
import com.aiworkspace.documents.domain.model.DocumentMetadata;
import com.aiworkspace.documents.domain.model.DocumentStatus;
import com.aiworkspace.shared.domain.DomainEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTest {

    private static final UUID DOC_ID = UUID.randomUUID();
    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final DocumentMetadata METADATA =
            new DocumentMetadata("application/pdf", 1024L, "/uploads/file.pdf");

    @Test
    void upload_createsDocumentWithUploadedStatus() {
        Document doc = Document.upload(DOC_ID, OWNER_ID, "Report Q1", METADATA);

        assertThat(doc.getId()).isEqualTo(DOC_ID);
        assertThat(doc.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(doc.getTitle()).isEqualTo("Report Q1");
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
        assertThat(doc.getChunkCount()).isNull();
        assertThat(doc.getIndexedAt()).isNull();
    }

    @Test
    void upload_registersDocumentUploadedEvent() {
        Document doc = Document.upload(DOC_ID, OWNER_ID, "Report Q1", METADATA);

        List<DomainEvent> events = doc.getDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(DocumentUploadedEvent.class);

        DocumentUploadedEvent event = (DocumentUploadedEvent) events.get(0);
        assertThat(event.documentId()).isEqualTo(DOC_ID);
        assertThat(event.ownerId()).isEqualTo(OWNER_ID);
        assertThat(event.title()).isEqualTo("Report Q1");
    }

    @Test
    void startIndexing_transitionsToIndexingStatus() {
        Document doc = Document.upload(DOC_ID, OWNER_ID, "Report", METADATA);
        doc.clearDomainEvents();

        doc.startIndexing();

        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.INDEXING);
        assertThat(doc.getDomainEvents()).isEmpty();
    }

    @Test
    void markAsIndexed_transitionsToIndexedWithChunkCount() {
        Document doc = Document.upload(DOC_ID, OWNER_ID, "Report", METADATA);
        doc.startIndexing();
        doc.clearDomainEvents();

        doc.markAsIndexed(42);

        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.INDEXED);
        assertThat(doc.getChunkCount()).isEqualTo(42);
        assertThat(doc.getIndexedAt()).isNotNull();
    }

    @Test
    void markAsIndexed_registersDocumentIndexedEvent() {
        Document doc = Document.upload(DOC_ID, OWNER_ID, "Report", METADATA);
        doc.startIndexing();
        doc.clearDomainEvents();

        doc.markAsIndexed(10);

        List<DomainEvent> events = doc.getDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(DocumentIndexedEvent.class);

        DocumentIndexedEvent event = (DocumentIndexedEvent) events.get(0);
        assertThat(event.chunkCount()).isEqualTo(10);
    }

    @Test
    void markAsFailed_transitionsToFailedWithErrorMessage() {
        Document doc = Document.upload(DOC_ID, OWNER_ID, "Report", METADATA);
        doc.startIndexing();
        doc.clearDomainEvents();

        doc.markAsFailed("PDF is encrypted");

        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.FAILED);
        assertThat(doc.getErrorMessage()).isEqualTo("PDF is encrypted");
        assertThat(doc.getDomainEvents()).isEmpty();
    }

    @Test
    void attachStoragePath_updatesMetadataPath() {
        Document doc = Document.upload(DOC_ID, OWNER_ID, "Report",
                new DocumentMetadata("application/pdf", 1024L, null));

        doc.attachStoragePath("/storage/abc123.pdf");

        assertThat(doc.getMetadata().storagePath()).isEqualTo("/storage/abc123.pdf");
    }

    @Test
    void stateMachine_uploadedCannotBeAccessedAsIndexed() {
        Document doc = Document.upload(DOC_ID, OWNER_ID, "Report", METADATA);

        // Should be UPLOADED, not yet INDEXED
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
        assertThat(doc.getStatus()).isNotEqualTo(DocumentStatus.INDEXED);
    }

    @Test
    void delete_registersDocumentDeletedEvent() {
        Document doc = Document.upload(DOC_ID, OWNER_ID, "Report", METADATA);
        doc.clearDomainEvents();

        doc.delete();

        assertThat(doc.getDomainEvents()).hasSize(1);
    }
}
