package com.aiworkspace.documents.application.service;

import com.aiworkspace.documents.application.command.DeleteDocumentCommand;
import com.aiworkspace.documents.application.command.UploadDocumentCommand;
import com.aiworkspace.documents.application.dto.DocumentDto;
import com.aiworkspace.documents.application.port.in.DeleteDocumentUseCase;
import com.aiworkspace.documents.application.port.in.GetDocumentUseCase;
import com.aiworkspace.documents.application.port.in.ListDocumentsUseCase;
import com.aiworkspace.documents.application.port.in.UploadDocumentUseCase;
import com.aiworkspace.documents.application.port.out.DocumentConfigPort;
import com.aiworkspace.documents.application.port.out.DocumentRepository;
import com.aiworkspace.documents.application.port.out.MimeTypeValidatorPort;
import com.aiworkspace.documents.application.port.out.StoragePort;
import com.aiworkspace.documents.domain.exception.DocumentNotFoundException;
import com.aiworkspace.documents.domain.exception.UnsupportedDocumentFormatException;
import com.aiworkspace.documents.domain.model.Document;
import com.aiworkspace.documents.domain.model.DocumentMetadata;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService implements UploadDocumentUseCase, ListDocumentsUseCase,
                                         GetDocumentUseCase, DeleteDocumentUseCase {

    private final DocumentRepository documentRepository;
    private final StoragePort storagePort;
    private final ApplicationEventPublisher eventPublisher;
    private final DocumentConfigPort config;
    private final MimeTypeValidatorPort mimeTypeValidator;
    private final Timer uploadTimer;

    public DocumentService(DocumentRepository documentRepository,
                            StoragePort storagePort,
                            ApplicationEventPublisher eventPublisher,
                            DocumentConfigPort config,
                            MimeTypeValidatorPort mimeTypeValidator,
                            MeterRegistry meterRegistry) {
        this.documentRepository = documentRepository;
        this.storagePort = storagePort;
        this.eventPublisher = eventPublisher;
        this.config = config;
        this.mimeTypeValidator = mimeTypeValidator;
        this.uploadTimer = Timer.builder("documents.upload.duration")
                .description("Time to accept and store an uploaded document")
                .register(meterRegistry);
    }

    @Override
    @Transactional
    public DocumentDto upload(UploadDocumentCommand command) {
        Timer.Sample sample = Timer.start();

        // Validate declared MIME type against allowlist
        if (!config.isAllowedMimeType(command.mimeType())) {
            throw new UnsupportedDocumentFormatException(command.mimeType());
        }

        // Validate actual file content via magic bytes — prevents MIME spoofing
        String detectedMime = mimeTypeValidator.detect(command.content());
        if (!config.isAllowedMimeType(detectedMime)) {
            throw new UnsupportedDocumentFormatException(
                    "File content detected as '" + detectedMime + "', which is not allowed");
        }

        if (command.content().length > config.getMaxFileSizeBytes()) {
            throw new UnsupportedDocumentFormatException(
                    "File size " + command.content().length + " exceeds maximum of "
                    + config.getMaxFileSizeBytes() + " bytes");
        }

        UUID documentId = UUID.randomUUID();

        // Store file first to get the path (storage is not transactional — acceptable for MVP)
        String storagePath = storagePort.store(
                documentId, command.ownerId().toString(),
                command.content(), command.fileName());

        DocumentMetadata metadata = new DocumentMetadata(
                command.mimeType(), command.content().length, storagePath);

        Document document = Document.upload(documentId, command.ownerId(), command.title(), metadata);
        Document saved = documentRepository.save(document);

        // Publish from original object — reconstituted 'saved' has no registered events
        document.getDomainEvents().forEach(eventPublisher::publishEvent);

        sample.stop(uploadTimer);
        return DocumentDto.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDto> listByOwner(UUID ownerId) {
        return documentRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(DocumentDto::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDto getById(UUID documentId, UUID ownerId) {
        return documentRepository.findByIdAndOwnerId(documentId, ownerId)
                .map(DocumentDto::from)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    @Override
    @Transactional
    public void delete(DeleteDocumentCommand command) {
        Document document = documentRepository.findByIdAndOwnerId(
                command.documentId(), command.ownerId())
                .orElseThrow(() -> new DocumentNotFoundException(command.documentId()));

        document.delete();
        document.getDomainEvents().forEach(eventPublisher::publishEvent);

        storagePort.delete(document.getMetadata().storagePath());
        documentRepository.delete(command.documentId());
    }
}
