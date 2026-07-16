package com.aiworkspace.aicore.application.port.out;

public interface ChunkingStrategyFactory {

    ChunkingStrategy getStrategy(String mimeType);
}
