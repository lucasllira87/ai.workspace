package com.aiworkspace.aicore.application.port.out.provider;

import com.aiworkspace.aicore.application.dto.EmbeddingRequest;
import com.aiworkspace.aicore.application.dto.EmbeddingResponse;
import com.aiworkspace.aicore.domain.model.ModelId;
import com.aiworkspace.aicore.domain.model.ProviderId;

import java.util.Set;

public interface EmbeddingProvider {

    ProviderId getProviderId();

    Set<ModelId> getSupportedModels();

    boolean supports(ModelId model);

    EmbeddingResponse embed(EmbeddingRequest request);
}
