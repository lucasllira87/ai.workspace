package com.aiworkspace.aicore.application.port.in;

import com.aiworkspace.aicore.application.command.GenerateEmbeddingCommand;
import com.aiworkspace.aicore.application.dto.EmbeddingResponse;

public interface GenerateEmbeddingUseCase {

    EmbeddingResponse execute(GenerateEmbeddingCommand command);
}
