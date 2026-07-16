package com.aiworkspace.aicore.application.port.out;

import com.aiworkspace.aicore.domain.model.Chunk;
import com.aiworkspace.aicore.domain.model.EmbeddingVector;

import java.util.List;
import java.util.UUID;

public interface VectorStore {

    void storeChunks(List<ChunkWithEmbedding> chunks);

    List<ChunkSearchResult> similaritySearch(UUID documentId, EmbeddingVector query,
                                              int limit, double threshold);

    void deleteByDocumentId(UUID documentId);

    record ChunkWithEmbedding(Chunk chunk, EmbeddingVector embedding) {}

    record ChunkSearchResult(Chunk chunk, double similarity) {}
}
