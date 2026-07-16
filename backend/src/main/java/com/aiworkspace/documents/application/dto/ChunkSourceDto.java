package com.aiworkspace.documents.application.dto;

import com.aiworkspace.documents.application.port.out.AIQueryPort;

import java.util.UUID;

public record ChunkSourceDto(
        UUID chunkId,
        String content,
        int chunkIndex,
        double similarity,
        String sectionTitle
) {
    public static ChunkSourceDto from(AIQueryPort.ChunkSource source) {
        return new ChunkSourceDto(
                source.chunkId(),
                source.content(),
                source.chunkIndex(),
                source.similarity(),
                source.sectionTitle());
    }
}
