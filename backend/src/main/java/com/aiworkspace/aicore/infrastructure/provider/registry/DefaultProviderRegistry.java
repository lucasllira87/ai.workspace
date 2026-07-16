package com.aiworkspace.aicore.infrastructure.provider.registry;

import com.aiworkspace.aicore.application.port.out.ProviderRegistry;
import com.aiworkspace.aicore.application.port.out.provider.ChatProvider;
import com.aiworkspace.aicore.application.port.out.provider.EmbeddingProvider;
import com.aiworkspace.aicore.domain.model.ProviderId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DefaultProviderRegistry implements ProviderRegistry {

    private final List<ChatProvider> chatProviders;
    private final List<EmbeddingProvider> embeddingProviders;

    public DefaultProviderRegistry(
            @Autowired(required = false) List<ChatProvider> chatProviders,
            @Autowired(required = false) List<EmbeddingProvider> embeddingProviders) {
        this.chatProviders = chatProviders != null ? chatProviders : List.of();
        this.embeddingProviders = embeddingProviders != null ? embeddingProviders : List.of();
    }

    @Override
    public List<ChatProvider> getChatProviders() {
        return chatProviders;
    }

    @Override
    public List<EmbeddingProvider> getEmbeddingProviders() {
        return embeddingProviders;
    }

    @Override
    public Optional<ChatProvider> findChatProvider(ProviderId id) {
        return chatProviders.stream()
                .filter(p -> p.getProviderId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<EmbeddingProvider> findEmbeddingProvider(ProviderId id) {
        return embeddingProviders.stream()
                .filter(p -> p.getProviderId().equals(id))
                .findFirst();
    }
}
