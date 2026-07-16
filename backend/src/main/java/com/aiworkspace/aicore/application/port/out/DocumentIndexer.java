package com.aiworkspace.aicore.application.port.out;

import java.util.UUID;

public interface DocumentIndexer {

    void markAsIndexed(UUID documentId, int chunkCount);

    void markAsError(UUID documentId, String errorMessage);
}
