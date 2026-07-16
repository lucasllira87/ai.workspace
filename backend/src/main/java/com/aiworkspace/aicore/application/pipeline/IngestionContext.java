package com.aiworkspace.aicore.application.pipeline;

import com.aiworkspace.aicore.application.port.out.DocumentParser.ParsedDocument;
import com.aiworkspace.aicore.application.port.out.VectorStore.ChunkWithEmbedding;
import com.aiworkspace.aicore.domain.model.Chunk;

import java.util.List;
import java.util.UUID;

public record IngestionContext(
        UUID documentId,
        String fileName,
        String mimeType,
        byte[] rawContent,
        String module,
        String requestedBy,
        ParsedDocument parsed,
        List<Chunk> chunks,
        List<ChunkWithEmbedding> chunksWithEmbeddings
) {
    public static IngestionContext initial(UUID documentId, String fileName, String mimeType,
                                           byte[] content, String module, String requestedBy) {
        return new IngestionContext(documentId, fileName, mimeType, content,
                module, requestedBy, null, null, null);
    }

    public IngestionContext withParsed(ParsedDocument parsed) {
        return new IngestionContext(documentId, fileName, mimeType, rawContent,
                module, requestedBy, parsed, null, null);
    }

    public IngestionContext withChunks(List<Chunk> chunks) {
        return new IngestionContext(documentId, fileName, mimeType, rawContent,
                module, requestedBy, parsed, chunks, null);
    }

    public IngestionContext withEmbeddings(List<ChunkWithEmbedding> embedded) {
        return new IngestionContext(documentId, fileName, mimeType, rawContent,
                module, requestedBy, parsed, chunks, embedded);
    }
}
