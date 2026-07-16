package com.aiworkspace.aicore.application.service;

import com.aiworkspace.aicore.application.command.IngestDocumentCommand;
import com.aiworkspace.aicore.application.dto.IngestionResultDto;
import com.aiworkspace.aicore.application.pipeline.IngestionPipeline;
import com.aiworkspace.aicore.application.pipeline.IngestionResult;
import com.aiworkspace.aicore.application.port.in.IngestDocumentUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IngestionService implements IngestDocumentUseCase {

    private final IngestionPipeline pipeline;

    public IngestionService(IngestionPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public IngestionResultDto execute(IngestDocumentCommand command) {
        IngestionResult result = pipeline.ingest(command);
        return new IngestionResultDto(
                result.documentId(),
                result.chunkCount(),
                result.totalTokens(),
                result.durationMs());
    }
}
