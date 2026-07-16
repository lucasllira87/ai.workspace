package com.aiworkspace.aicore.application.orchestrator;

import com.aiworkspace.aicore.application.port.out.AIConfigPort;
import com.aiworkspace.aicore.application.port.out.ProviderRegistry;
import com.aiworkspace.aicore.application.port.out.provider.ChatProvider;
import com.aiworkspace.aicore.application.port.out.provider.EmbeddingProvider;
import com.aiworkspace.aicore.domain.model.ModelId;
import com.aiworkspace.aicore.domain.model.ProviderId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FallbackChain {

    private final ProviderRegistry registry;
    private final AIConfigPort aiConfig;

    public FallbackChain(ProviderRegistry registry, AIConfigPort aiConfig) {
        this.registry = registry;
        this.aiConfig = aiConfig;
    }

    public List<ChatProvider> getChatProvidersInOrder(ModelId preferredModel) {
        List<String> order = aiConfig.getChatFallbackOrder();
        return order.stream()
                .map(id -> registry.findChatProvider(ProviderId.of(id)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(p -> preferredModel == null || p.supports(preferredModel))
                .toList();
    }

    public List<EmbeddingProvider> getEmbeddingProvidersInOrder(ModelId preferredModel) {
        List<String> order = aiConfig.getEmbeddingFallbackOrder();
        return order.stream()
                .map(id -> registry.findEmbeddingProvider(ProviderId.of(id)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(p -> preferredModel == null || p.supports(preferredModel))
                .toList();
    }
}
