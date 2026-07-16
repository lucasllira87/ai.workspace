package com.aiworkspace.aicore.domain.model;

public record TokenUsage(int promptTokens, int completionTokens) {

    public int total() {
        return promptTokens + completionTokens;
    }

    public static TokenUsage of(int promptTokens, int completionTokens) {
        return new TokenUsage(promptTokens, completionTokens);
    }

    public static TokenUsage embeddingUsage(int tokens) {
        return new TokenUsage(tokens, 0);
    }

    public static TokenUsage zero() {
        return new TokenUsage(0, 0);
    }
}
