package com.aiworkspace.documents.application.service;

import com.aiworkspace.documents.application.port.out.AIIngestionPort;
import com.aiworkspace.documents.application.port.out.DocumentRepository;
import com.aiworkspace.documents.application.port.out.StoragePort;
import com.aiworkspace.documents.domain.event.DocumentUploadedEvent;
import com.aiworkspace.documents.domain.model.Document;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.TimeUnit;

@Component
public class DocumentIndexingListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentIndexingListener.class);

    private final DocumentRepository documentRepository;
    private final StoragePort storagePort;
    private final AIIngestionPort aiIngestionPort;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final Timer indexingTimer;
    private final Counter indexingFailureCounter;

    public DocumentIndexingListener(DocumentRepository documentRepository,
                                     StoragePort storagePort,
                                     AIIngestionPort aiIngestionPort,
                                     ApplicationEventPublisher eventPublisher,
                                     TransactionTemplate transactionTemplate,
                                     MeterRegistry meterRegistry) {
        this.documentRepository = documentRepository;
        this.storagePort = storagePort;
        this.aiIngestionPort = aiIngestionPort;
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = transactionTemplate;
        this.indexingTimer = Timer.builder("documents.indexing.duration")
                .description("Time to index a document end-to-end")
                .register(meterRegistry);
        this.indexingFailureCounter = Counter.builder("documents.indexing.failures")
                .description("Number of document indexing failures")
                .register(meterRegistry);
    }

    @Async("documentIndexingExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentUploaded(DocumentUploadedEvent event) {
        log.info("Starting indexing for document {} ({})", event.documentId(), event.title());
        long startMs = System.currentTimeMillis();

        // Transition UPLOADED → INDEXING via aggregate
        transactionTemplate.execute(status -> {
            documentRepository.findByIdAndOwnerId(event.documentId(), event.ownerId())
                    .ifPresent(doc -> {
                        doc.startIndexing();
                        documentRepository.save(doc);
                    });
            return null;
        });

        try {
            byte[] content = storagePort.retrieve(event.storagePath());

            AIIngestionPort.IngestionResult result = aiIngestionPort.ingest(
                    event.documentId(),
                    content,
                    event.mimeType(),
                    event.title(),
                    "documents",
                    event.ownerId().toString());

            // Transition INDEXING → INDEXED via aggregate; fires DocumentIndexedEvent
            transactionTemplate.execute(status -> {
                documentRepository.findByIdAndOwnerId(event.documentId(), event.ownerId())
                        .ifPresent(doc -> {
                            doc.markAsIndexed(result.chunkCount());
                            documentRepository.save(doc);
                            // Publish from original object — reconstituted save() has no events
                            doc.getDomainEvents().forEach(eventPublisher::publishEvent);
                        });
                return null;
            });

            long durationMs = System.currentTimeMillis() - startMs;
            indexingTimer.record(durationMs, TimeUnit.MILLISECONDS);
            log.info("Indexing completed for document {} — {} chunks in {}ms",
                    event.documentId(), result.chunkCount(), durationMs);

        } catch (Exception e) {
            indexingFailureCounter.increment();
            log.error("Indexing failed for document {}: {}", event.documentId(), e.getMessage(), e);

            // Transition → FAILED via aggregate
            transactionTemplate.execute(status -> {
                documentRepository.findByIdAndOwnerId(event.documentId(), event.ownerId())
                        .ifPresent(doc -> {
                            doc.markAsFailed(e.getMessage());
                            documentRepository.save(doc);
                        });
                return null;
            });
        }
    }
}
