package com.aiworkspace.aicore.application.pipeline;

import java.util.UUID;

public record IngestionResult(
        UUID documentId,
        int chunkCount,
        int totalTokens,
        long durationMs
) {}
