package com.aiworkspace.aicore.infrastructure.chunking;

import com.aiworkspace.aicore.application.port.out.ChunkingStrategy;
import com.aiworkspace.aicore.application.port.out.ChunkingStrategyFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultChunkingStrategyFactory implements ChunkingStrategyFactory {

    private final List<ChunkingStrategy> strategies;
    private final FixedSizeChunkingStrategy fallback;

    public DefaultChunkingStrategyFactory(List<ChunkingStrategy> strategies,
                                           FixedSizeChunkingStrategy fallback) {
        this.strategies = strategies;
        this.fallback = fallback;
    }

    @Override
    public ChunkingStrategy getStrategy(String mimeType) {
        return strategies.stream()
                .filter(s -> s.supports(mimeType)
                        && !(s instanceof FixedSizeChunkingStrategy))
                .findFirst()
                .orElse(fallback);
    }
}
