package com.aiworkspace.aicore.application.port.out;

import com.aiworkspace.aicore.application.port.out.provider.ChatProvider;
import com.aiworkspace.aicore.application.port.out.provider.EmbeddingProvider;
import com.aiworkspace.aicore.domain.model.ProviderId;

import java.util.List;
import java.util.Optional;

public interface ProviderRegistry {

    List<ChatProvider> getChatProviders();

    List<EmbeddingProvider> getEmbeddingProviders();

    Optional<ChatProvider> findChatProvider(ProviderId id);

    Optional<EmbeddingProvider> findEmbeddingProvider(ProviderId id);
}
