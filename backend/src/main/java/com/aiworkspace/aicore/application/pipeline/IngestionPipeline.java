package com.aiworkspace.aicore.application.pipeline;

import com.aiworkspace.aicore.application.command.IngestDocumentCommand;
import com.aiworkspace.aicore.application.dto.EmbeddingRequest;
import com.aiworkspace.aicore.application.dto.EmbeddingResponse;
import com.aiworkspace.aicore.application.dto.UsageMetricEvent;
import com.aiworkspace.aicore.application.orchestrator.AIOrchestrator;
import com.aiworkspace.aicore.application.port.out.ChunkingStrategy.ChunkingOptions;
import com.aiworkspace.aicore.application.port.out.ChunkingStrategyFactory;
import com.aiworkspace.aicore.application.port.out.DocumentParser;
import com.aiworkspace.aicore.application.port.out.DocumentParser.ParsedDocument;
import com.aiworkspace.aicore.application.port.out.AIConfigPort;
import com.aiworkspace.aicore.application.port.out.UsageMetricsPort;
import com.aiworkspace.aicore.application.port.out.VectorStore;
import com.aiworkspace.aicore.application.port.out.VectorStore.ChunkWithEmbedding;
import com.aiworkspace.aicore.domain.event.DocumentIngestionCompletedEvent;
import com.aiworkspace.aicore.domain.exception.IngestionException;
import com.aiworkspace.aicore.domain.model.Chunk;
import com.aiworkspace.aicore.domain.model.ModelId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class IngestionPipeline {

    private static final Logger log = LoggerFactory.getLogger(IngestionPipeline.class);

    private final List<DocumentParser> parsers;
    private final ChunkingStrategyFactory chunkingFactory;
    private final AIOrchestrator orchestrator;
    private final VectorStore vectorStore;
    private final UsageMetricsPort metricsPort;
    private final ApplicationEventPublisher eventPublisher;
    private final AIConfigPort aiConfig;

    public IngestionPipeline(List<DocumentParser> parsers,
                              ChunkingStrategyFactory chunkingFactory,
                              AIOrchestrator orchestrator,
                              VectorStore vectorStore,
                              UsageMetricsPort metricsPort,
                              ApplicationEventPublisher eventPublisher,
                              AIConfigPort aiConfig) {
        this.parsers = parsers;
        this.chunkingFactory = chunkingFactory;
        this.orchestrator = orchestrator;
        this.vectorStore = vectorStore;
        this.metricsPort = metricsPort;
        this.eventPublisher = eventPublisher;
        this.aiConfig = aiConfig;
    }

    public IngestionResult ingest(IngestDocumentCommand command) {
        long startMs = System.currentTimeMillis();

        try {
            // Stage 1: Parse
            DocumentParser parser = findParser(command.mimeType());
            ParsedDocument parsed = parser.parse(command.content(), command.fileName());
            log.debug("Stage 1 — Parsed document {}: {} chars, {} pages",
                    command.documentId(), parsed.text().length(), parsed.pageCount());

            // Stage 2: Chunk
            var strategy = chunkingFactory.getStrategy(command.mimeType());
            var options = new ChunkingOptions(
                    aiConfig.getChunkSize(),
                    aiConfig.getChunkOverlap(),
                    strategy.getClass().getSimpleName());
            List<Chunk> chunks = strategy.chunk(command.documentId(), parsed, options);
            log.debug("Stage 2 — Chunked into {} chunks", chunks.size());

            if (chunks.isEmpty()) {
                return new IngestionResult(command.documentId(), 0, 0,
                        System.currentTimeMillis() - startMs);
            }

            // Stage 3: Generate embeddings in batches
            int batchSize = aiConfig.getEmbeddingBatchSize();
            ModelId embeddingModel = ModelId.of(aiConfig.getDefaultEmbeddingModel());
            List<ChunkWithEmbedding> allChunksWithEmbeddings = new ArrayList<>();
            int totalTokens = 0;

            for (int i = 0; i < chunks.size(); i += batchSize) {
                List<Chunk> batch = chunks.subList(i, Math.min(i + batchSize, chunks.size()));
                List<String> texts = batch.stream().map(Chunk::content).toList();

                EmbeddingRequest request = new EmbeddingRequest(embeddingModel, texts,
                        Map.of("module", command.module(), "documentId",
                                command.documentId().toString()));
                EmbeddingResponse response = orchestrator.embed(request);

                for (int j = 0; j < batch.size(); j++) {
                    allChunksWithEmbeddings.add(
                            new ChunkWithEmbedding(batch.get(j), response.embeddings().get(j)));
                }
                totalTokens += response.usage().total();
                log.debug("Stage 3 — Embedded batch {}/{}", (i / batchSize) + 1,
                        (int) Math.ceil((double) chunks.size() / batchSize));
            }

            // Stage 4: Store in vector store
            vectorStore.storeChunks(allChunksWithEmbeddings);
            log.debug("Stage 4 — Stored {} vectors", allChunksWithEmbeddings.size());

            long durationMs = System.currentTimeMillis() - startMs;

            // Stage 5: Record ingestion metrics
            metricsPort.record(UsageMetricEvent.forIngestion(
                    command.documentId(), command.module(), command.requestedBy(),
                    totalTokens, durationMs));

            // Stage 6: Publish domain event
            eventPublisher.publishEvent(new DocumentIngestionCompletedEvent(
                    command.documentId(), chunks.size(), totalTokens, Instant.now()));

            log.info("Ingestion completed for document {} — {} chunks, {} tokens, {}ms",
                    command.documentId(), chunks.size(), totalTokens, durationMs);

            return new IngestionResult(command.documentId(), chunks.size(), totalTokens, durationMs);

        } catch (IngestionException e) {
            throw e;
        } catch (Exception e) {
            throw new IngestionException(
                    "Failed to ingest document " + command.documentId() + ": " + e.getMessage(), e);
        }
    }

    private DocumentParser findParser(String mimeType) {
        return parsers.stream()
                .filter(p -> p.supports(mimeType))
                .findFirst()
                .orElseThrow(() -> new IngestionException(
                        "No parser available for MIME type: " + mimeType));
    }
}
