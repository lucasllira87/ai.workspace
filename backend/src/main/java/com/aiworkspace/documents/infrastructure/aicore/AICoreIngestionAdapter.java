package com.aiworkspace.documents.infrastructure.aicore;

import com.aiworkspace.aicore.application.command.IngestDocumentCommand;
import com.aiworkspace.aicore.application.dto.IngestionResultDto;
import com.aiworkspace.aicore.application.port.in.IngestDocumentUseCase;
import com.aiworkspace.documents.application.port.out.AIIngestionPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AICoreIngestionAdapter implements AIIngestionPort {

    private final IngestDocumentUseCase ingestDocumentUseCase;

    public AICoreIngestionAdapter(IngestDocumentUseCase ingestDocumentUseCase) {
        this.ingestDocumentUseCase = ingestDocumentUseCase;
    }

    @Override
    public IngestionResult ingest(UUID documentId, byte[] content, String mimeType,
                                   String fileName, String module, String requestedBy) {
        IngestDocumentCommand command = new IngestDocumentCommand(
                documentId, fileName, mimeType, content, module, requestedBy);

        IngestionResultDto result = ingestDocumentUseCase.execute(command);

        return new IngestionResult(result.chunkCount(), result.totalTokens(), result.durationMs());
    }
}
