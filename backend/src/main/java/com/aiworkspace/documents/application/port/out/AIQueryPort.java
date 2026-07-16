package com.aiworkspace.documents.application.port.out;

import java.util.List;
import java.util.UUID;

public interface AIQueryPort {

    List<ChunkSource> findRelevantChunks(UUID documentId, String question,
                                          int limit, double threshold);

    String chatWithContext(String question, String context, String documentTitle);

    record ChunkSource(UUID chunkId, String content, int chunkIndex,
                       double similarity, String sectionTitle) {}
}
