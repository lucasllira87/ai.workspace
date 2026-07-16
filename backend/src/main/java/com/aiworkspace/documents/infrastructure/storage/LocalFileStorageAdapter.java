package com.aiworkspace.documents.infrastructure.storage;

import com.aiworkspace.documents.application.port.out.StoragePort;
import com.aiworkspace.documents.domain.exception.DocumentStorageException;
import com.aiworkspace.documents.infrastructure.config.DocumentsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Component
public class LocalFileStorageAdapter implements StoragePort {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageAdapter.class);

    private final Path basePath;

    public LocalFileStorageAdapter(DocumentsProperties properties) {
        this.basePath = Path.of(properties.getStorage().getLocalBasePath());
    }

    @Override
    public String store(UUID documentId, String ownerId, byte[] content, String originalFileName) {
        try {
            Path dir = basePath.resolve(ownerId).resolve(documentId.toString());
            Files.createDirectories(dir);

            String safeFileName = sanitizeFileName(originalFileName);
            Path filePath = dir.resolve(safeFileName);
            Files.write(filePath, content, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            return filePath.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new DocumentStorageException(
                    "Failed to store document " + documentId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] retrieve(String storagePath) {
        try {
            return Files.readAllBytes(Path.of(storagePath));
        } catch (IOException e) {
            throw new DocumentStorageException(
                    "Failed to retrieve document at " + storagePath + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String storagePath) {
        if (storagePath == null) return;
        try {
            Path path = Path.of(storagePath);
            Files.deleteIfExists(path);

            // Remove parent dir if empty (documentId dir)
            Path parent = path.getParent();
            if (parent != null && Files.isDirectory(parent)) {
                try (var entries = Files.list(parent)) {
                    if (entries.findFirst().isEmpty()) {
                        Files.delete(parent);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Failed to delete file at {}: {}", storagePath, e.getMessage());
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "document";
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
