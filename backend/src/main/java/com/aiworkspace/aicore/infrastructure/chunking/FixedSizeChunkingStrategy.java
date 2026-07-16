package com.aiworkspace.aicore.infrastructure.chunking;

import com.aiworkspace.aicore.application.port.out.ChunkingStrategy;
import com.aiworkspace.aicore.application.port.out.DocumentParser.ParsedDocument;
import com.aiworkspace.aicore.domain.model.Chunk;
import com.aiworkspace.aicore.domain.model.ChunkMetadata;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Splits text into fixed-size chunks measured in estimated tokens (1 token ≈ 4 chars).
 * Supports configurable overlap between consecutive chunks.
 */
@Component
public class FixedSizeChunkingStrategy implements ChunkingStrategy {

    private static final int CHARS_PER_TOKEN = 4;

    @Override
    public boolean supports(String mimeType) {
        return true; // fallback strategy for any format
    }

    @Override
    public List<Chunk> chunk(UUID documentId, ParsedDocument document, ChunkingOptions options) {
        String text = document.text();
        if (text == null || text.isBlank()) return List.of();

        int chunkChars = options.chunkSize() * CHARS_PER_TOKEN;
        int overlapChars = options.overlapSize() * CHARS_PER_TOKEN;
        int step = chunkChars - overlapChars;

        List<Chunk> chunks = new ArrayList<>();
        int index = 0;
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkChars, text.length());

            // Extend to word boundary if not at end
            if (end < text.length()) {
                int spacePos = text.lastIndexOf(' ', end);
                if (spacePos > start) end = spacePos;
            }

            String chunkContent = text.substring(start, end).strip();
            if (!chunkContent.isBlank()) {
                int estimatedTokens = chunkContent.length() / CHARS_PER_TOKEN;
                ChunkMetadata metadata = ChunkMetadata.of(
                        index, start, end, estimatedTokens, options.strategyName());
                chunks.add(Chunk.of(documentId, chunkContent, metadata));
                index++;
            }

            start += step;
            if (start >= end) start = end; // avoid infinite loop on very short step
        }

        return chunks;
    }
}
