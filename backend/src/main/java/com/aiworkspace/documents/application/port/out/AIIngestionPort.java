package com.aiworkspace.documents.application.port.out;

import java.util.UUID;

public interface AIIngestionPort {

    IngestionResult ingest(UUID documentId, byte[] content, String mimeType,
                           String fileName, String module, String requestedBy);

    record IngestionResult(int chunkCount, int totalTokens, long durationMs) {}
}
