package com.aiworkspace.aicore.application.service;

import com.aiworkspace.aicore.application.command.GenerateEmbeddingCommand;
import com.aiworkspace.aicore.application.dto.EmbeddingRequest;
import com.aiworkspace.aicore.application.dto.EmbeddingResponse;
import com.aiworkspace.aicore.application.orchestrator.AIOrchestrator;
import com.aiworkspace.aicore.application.port.in.GenerateEmbeddingUseCase;
import com.aiworkspace.aicore.application.port.out.AIConfigPort;
import com.aiworkspace.aicore.domain.model.ModelId;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmbeddingService implements GenerateEmbeddingUseCase {

    private final AIOrchestrator orchestrator;
    private final AIConfigPort aiConfig;

    public EmbeddingService(AIOrchestrator orchestrator, AIConfigPort aiConfig) {
        this.orchestrator = orchestrator;
        this.aiConfig = aiConfig;
    }

    @Override
    public EmbeddingResponse execute(GenerateEmbeddingCommand command) {
        ModelId model = command.preferredModel() != null
                ? command.preferredModel()
                : ModelId.of(aiConfig.getDefaultEmbeddingModel());

        EmbeddingRequest request = new EmbeddingRequest(model, command.texts(),
                Map.of("module", command.module() != null ? command.module() : "unknown",
                       "requestedBy", command.requestedBy() != null ? command.requestedBy() : ""));

        return orchestrator.embed(request);
    }
}
