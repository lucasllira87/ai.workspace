package com.aiworkspace.aicore.application.port.out;

import com.aiworkspace.aicore.domain.model.Chunk;

import java.util.List;
import java.util.UUID;

public interface ChunkingStrategy {

    boolean supports(String mimeType);

    List<Chunk> chunk(UUID documentId, DocumentParser.ParsedDocument document, ChunkingOptions options);

    record ChunkingOptions(int chunkSize, int overlapSize, String strategyName) {
        public static ChunkingOptions defaults() {
            return new ChunkingOptions(500, 50, "fixed-size");
        }
    }
}
