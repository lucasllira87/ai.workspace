package com.aiworkspace.documents.application.dto;

import java.util.List;

public record ChatWithDocumentDto(
        String answer,
        List<ChunkSourceDto> sources,
        boolean hasContext
) {
    public static ChatWithDocumentDto withSources(String answer, List<ChunkSourceDto> sources) {
        return new ChatWithDocumentDto(answer, sources, true);
    }

    public static ChatWithDocumentDto noContext(String answer) {
        return new ChatWithDocumentDto(answer, List.of(), false);
    }
}
