package com.aiworkspace.aicore.domain.model;

public record ChunkMetadata(
        int chunkIndex,
        int startOffset,
        int endOffset,
        int estimatedTokenCount,
        Integer pageNumber,
        String sectionTitle,
        String chunkingStrategy
) {
    public static ChunkMetadata of(int index, int start, int end, int tokens, String strategy) {
        return new ChunkMetadata(index, start, end, tokens, null, null, strategy);
    }

    public static ChunkMetadata withPage(int index, int start, int end, int tokens,
                                          Integer page, String section, String strategy) {
        return new ChunkMetadata(index, start, end, tokens, page, section, strategy);
    }
}
