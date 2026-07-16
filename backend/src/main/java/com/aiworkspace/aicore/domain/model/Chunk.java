package com.aiworkspace.aicore.domain.model;

import java.util.UUID;

public record Chunk(
        UUID id,
        UUID documentId,
        String content,
        ChunkMetadata metadata
) {
    public static Chunk of(UUID documentId, String content, ChunkMetadata metadata) {
        return new Chunk(UUID.randomUUID(), documentId, content, metadata);
    }
}
