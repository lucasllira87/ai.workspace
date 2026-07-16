package com.aiworkspace.aicore.infrastructure.chunking;

import com.aiworkspace.aicore.application.port.out.ChunkingStrategy;
import com.aiworkspace.aicore.application.port.out.DocumentParser.ParsedDocument;
import com.aiworkspace.aicore.domain.model.Chunk;
import com.aiworkspace.aicore.domain.model.ChunkMetadata;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits Markdown documents by heading sections (H1–H3).
 * Falls back to FixedSizeChunkingStrategy if a section is too large.
 */
@Component
public class MarkdownHeaderChunkingStrategy implements ChunkingStrategy {

    private static final Pattern HEADER_PATTERN =
            Pattern.compile("^(#{1,3})\\s+(.+)$", Pattern.MULTILINE);
    private static final int CHARS_PER_TOKEN = 4;

    private final FixedSizeChunkingStrategy fallback;

    public MarkdownHeaderChunkingStrategy(FixedSizeChunkingStrategy fallback) {
        this.fallback = fallback;
    }

    @Override
    public boolean supports(String mimeType) {
        return "text/markdown".equalsIgnoreCase(mimeType)
                || "text/x-markdown".equalsIgnoreCase(mimeType);
    }

    @Override
    public List<Chunk> chunk(UUID documentId, ParsedDocument document, ChunkingOptions options) {
        String text = document.text();
        if (text == null || text.isBlank()) return List.of();

        List<Chunk> chunks = new ArrayList<>();
        Matcher matcher = HEADER_PATTERN.matcher(text);

        List<int[]> headerPositions = new ArrayList<>();
        List<String> headerTitles = new ArrayList<>();
        while (matcher.find()) {
            headerPositions.add(new int[]{matcher.start(), matcher.end()});
            headerTitles.add(matcher.group(2).strip());
        }

        if (headerPositions.isEmpty()) {
            return fallback.chunk(documentId, document, options);
        }

        int chunkIndex = 0;
        for (int i = 0; i < headerPositions.size(); i++) {
            int sectionStart = headerPositions.get(i)[0];
            int sectionEnd = (i + 1 < headerPositions.size())
                    ? headerPositions.get(i + 1)[0]
                    : text.length();

            String sectionContent = text.substring(sectionStart, sectionEnd).strip();
            String title = headerTitles.get(i);
            int maxChunkChars = options.chunkSize() * CHARS_PER_TOKEN;

            if (sectionContent.length() <= maxChunkChars) {
                int estimatedTokens = sectionContent.length() / CHARS_PER_TOKEN;
                ChunkMetadata metadata = ChunkMetadata.withPage(
                        chunkIndex, sectionStart, sectionEnd, estimatedTokens,
                        null, title, options.strategyName());
                chunks.add(Chunk.of(documentId, sectionContent, metadata));
                chunkIndex++;
            } else {
                // Section too large — split with fixed-size fallback
                ParsedDocument subDoc = new ParsedDocument(sectionContent,
                        document.mimeType(), 1, document.metadata());
                List<Chunk> subChunks = fallback.chunk(documentId, subDoc, options);
                for (Chunk sub : subChunks) {
                    ChunkMetadata meta = ChunkMetadata.withPage(
                            chunkIndex,
                            sub.metadata().startOffset() + sectionStart,
                            sub.metadata().endOffset() + sectionStart,
                            sub.metadata().estimatedTokenCount(),
                            null, title, options.strategyName());
                    chunks.add(Chunk.of(documentId, sub.content(), meta));
                    chunkIndex++;
                }
            }
        }

        return chunks;
    }
}
