package com.aiworkspace.documents.application.port.out;

import java.util.UUID;

public interface StoragePort {
    String store(UUID documentId, String ownerId, byte[] content, String originalFileName);
    byte[] retrieve(String storagePath);
    void delete(String storagePath);
}
