package com.aiworkspace.aicore.application.port.out;

import java.math.BigDecimal;
import java.util.List;

public interface AIConfigPort {

    String getDefaultChatModel();

    String getDefaultEmbeddingModel();

    List<String> getChatFallbackOrder();

    List<String> getEmbeddingFallbackOrder();

    boolean isCostGuardEnabled();

    BigDecimal getMaxCostPerRequest();

    int getChunkSize();

    int getChunkOverlap();

    int getEmbeddingBatchSize();
}
