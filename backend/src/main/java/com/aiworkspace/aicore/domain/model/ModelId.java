package com.aiworkspace.aicore.domain.model;

import com.aiworkspace.shared.exception.DomainException;

import java.util.Objects;

public record ModelId(String value) {

    public ModelId {
        Objects.requireNonNull(value, "ModelId cannot be null");
        if (value.isBlank()) throw new DomainException("ModelId cannot be blank");
    }

    public static ModelId of(String value) {
        return new ModelId(value);
    }

    // OpenAI chat models
    public static final ModelId GPT_4O = new ModelId("gpt-4o");
    public static final ModelId GPT_4O_MINI = new ModelId("gpt-4o-mini");

    // OpenAI embedding models
    public static final ModelId TEXT_EMBEDDING_3_SMALL = new ModelId("text-embedding-3-small");
    public static final ModelId TEXT_EMBEDDING_3_LARGE = new ModelId("text-embedding-3-large");

    // Anthropic models
    public static final ModelId CLAUDE_OPUS_4 = new ModelId("claude-opus-4-8");
    public static final ModelId CLAUDE_SONNET_4 = new ModelId("claude-sonnet-4-5");
    public static final ModelId CLAUDE_HAIKU_4 = new ModelId("claude-haiku-4-5");

    // Google models
    public static final ModelId GEMINI_2_FLASH = new ModelId("gemini-2.0-flash");
    public static final ModelId GEMINI_1_5_PRO = new ModelId("gemini-1.5-pro");
    public static final ModelId GEMINI_EMBEDDING = new ModelId("text-embedding-004");

    @Override
    public String toString() {
        return value;
    }
}
