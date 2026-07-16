package com.aiworkspace.documents.domain.model;

public record DocumentMetadata(
        String mimeType,
        long sizeBytes,
        String storagePath
) {
    public DocumentMetadata withStoragePath(String path) {
        return new DocumentMetadata(mimeType, sizeBytes, path);
    }
}
