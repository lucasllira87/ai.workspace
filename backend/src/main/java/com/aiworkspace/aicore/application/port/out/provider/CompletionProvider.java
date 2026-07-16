package com.aiworkspace.aicore.application.port.out.provider;

import com.aiworkspace.aicore.application.dto.CompletionRequest;
import com.aiworkspace.aicore.application.dto.CompletionResponse;
import com.aiworkspace.aicore.domain.model.ModelId;
import com.aiworkspace.aicore.domain.model.ProviderId;

import java.util.Set;

public interface CompletionProvider {

    ProviderId getProviderId();

    Set<ModelId> getSupportedModels();

    boolean supports(ModelId model);

    CompletionResponse complete(CompletionRequest request);
}
