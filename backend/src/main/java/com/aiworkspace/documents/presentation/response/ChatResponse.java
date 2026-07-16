package com.aiworkspace.documents.presentation.response;

import com.aiworkspace.documents.application.dto.ChatWithDocumentDto;
import com.aiworkspace.documents.application.dto.ChunkSourceDto;

import java.util.List;
import java.util.UUID;

public record ChatResponse(
        String answer,
        boolean hasContext,
        List<ChunkSourceResponse> sources
) {
    public static ChatResponse from(ChatWithDocumentDto dto) {
        return new ChatResponse(
                dto.answer(),
                dto.hasContext(),
                dto.sources().stream().map(ChunkSourceResponse::from).toList());
    }

    public record ChunkSourceResponse(
            UUID chunkId,
            String content,
            int chunkIndex,
            double similarity,
            String sectionTitle
    ) {
        public static ChunkSourceResponse from(ChunkSourceDto dto) {
            return new ChunkSourceResponse(
                    dto.chunkId(), dto.content(), dto.chunkIndex(),
                    dto.similarity(), dto.sectionTitle());
        }
    }
}
