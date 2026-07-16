package com.aiworkspace.aicore.application.port.out.provider;

import com.aiworkspace.aicore.application.dto.ChatRequest;
import com.aiworkspace.aicore.application.dto.ChatResponse;
import com.aiworkspace.aicore.domain.model.ModelId;
import com.aiworkspace.aicore.domain.model.ProviderId;

import java.util.Set;

public interface ChatProvider {

    ProviderId getProviderId();

    Set<ModelId> getSupportedModels();

    boolean supports(ModelId model);

    ChatResponse chat(ChatRequest request);
}
