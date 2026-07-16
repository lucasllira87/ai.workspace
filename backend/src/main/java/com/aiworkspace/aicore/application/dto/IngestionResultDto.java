package com.aiworkspace.aicore.application.dto;

import java.util.UUID;

public record IngestionResultDto(
        UUID documentId,
        int chunkCount,
        int totalTokens,
        long durationMs
) {}
