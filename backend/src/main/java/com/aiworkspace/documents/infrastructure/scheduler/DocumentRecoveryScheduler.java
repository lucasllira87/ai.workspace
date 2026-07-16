package com.aiworkspace.documents.infrastructure.scheduler;

import com.aiworkspace.documents.application.port.out.DocumentConfigPort;
import com.aiworkspace.documents.application.port.out.DocumentRepository;
import com.aiworkspace.documents.domain.model.Document;
import com.aiworkspace.documents.domain.model.DocumentStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Recovers documents stuck in UPLOADED or INDEXING status due to process crashes or
 * async event delivery failures. Runs every 5 minutes.
 */
@Component
public class DocumentRecoveryScheduler {

    private static final Logger log = LoggerFactory.getLogger(DocumentRecoveryScheduler.class);

    private final DocumentRepository documentRepository;
    private final DocumentConfigPort config;
    private final Counter recoveredCounter;

    public DocumentRecoveryScheduler(DocumentRepository documentRepository,
                                      DocumentConfigPort config,
                                      MeterRegistry meterRegistry) {
        this.documentRepository = documentRepository;
        this.config = config;
        this.recoveredCounter = Counter.builder("documents.recovery.triggered")
                .description("Number of stuck documents recovered by the scheduler")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${documents.recovery.interval-ms:300000}")
    @Transactional
    public void recoverStuckDocuments() {
        Instant cutoff = Instant.now()
                .minus(config.getStuckDocumentThresholdMinutes(), ChronoUnit.MINUTES);

        List<Document> stuckUploaded = documentRepository.findStuckDocuments(
                DocumentStatus.UPLOADED, cutoff);
        List<Document> stuckIndexing = documentRepository.findStuckDocuments(
                DocumentStatus.INDEXING, cutoff);

        int total = stuckUploaded.size() + stuckIndexing.size();
        if (total == 0) {
            return;
        }

        log.warn("Recovery: found {} stuck documents (UPLOADED={}, INDEXING={})",
                total, stuckUploaded.size(), stuckIndexing.size());

        for (Document doc : stuckUploaded) {
            doc.markAsFailed("Indexing did not start within the expected time window");
            documentRepository.save(doc);
            recoveredCounter.increment();
            log.warn("Marked document {} as FAILED (was stuck in UPLOADED)", doc.getId());
        }

        for (Document doc : stuckIndexing) {
            doc.markAsFailed("Indexing did not complete within the expected time window");
            documentRepository.save(doc);
            recoveredCounter.increment();
            log.warn("Marked document {} as FAILED (was stuck in INDEXING)", doc.getId());
        }
    }
}
