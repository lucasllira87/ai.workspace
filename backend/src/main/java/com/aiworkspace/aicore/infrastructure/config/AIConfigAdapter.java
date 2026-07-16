package com.aiworkspace.aicore.infrastructure.config;

import com.aiworkspace.aicore.application.port.out.AIConfigPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class AIConfigAdapter implements AIConfigPort {

    private final AIProperties properties;

    public AIConfigAdapter(AIProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getDefaultChatModel() {
        return properties.getDefaultChatModel();
    }

    @Override
    public String getDefaultEmbeddingModel() {
        return properties.getDefaultEmbeddingModel();
    }

    @Override
    public List<String> getChatFallbackOrder() {
        return properties.getFallbackOrder().getChat();
    }

    @Override
    public List<String> getEmbeddingFallbackOrder() {
        return properties.getFallbackOrder().getEmbedding();
    }

    @Override
    public boolean isCostGuardEnabled() {
        return properties.getCostGuard().isEnabled();
    }

    @Override
    public BigDecimal getMaxCostPerRequest() {
        return properties.getCostGuard().getMaxCostPerRequest();
    }

    @Override
    public int getChunkSize() {
        return properties.getIngestion().getChunkSize();
    }

    @Override
    public int getChunkOverlap() {
        return properties.getIngestion().getChunkOverlap();
    }

    @Override
    public int getEmbeddingBatchSize() {
        return properties.getIngestion().getEmbeddingBatchSize();
    }
}
