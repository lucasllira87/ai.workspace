package com.aiworkspace.aicore.domain.model;

import com.aiworkspace.shared.exception.DomainException;

import java.util.Objects;

public record ProviderId(String value) {

    public ProviderId {
        Objects.requireNonNull(value, "ProviderId cannot be null");
        if (value.isBlank()) throw new DomainException("ProviderId cannot be blank");
    }

    public static ProviderId of(String value) {
        return new ProviderId(value);
    }

    public static final ProviderId OPENAI = new ProviderId("openai");
    public static final ProviderId ANTHROPIC = new ProviderId("anthropic");
    public static final ProviderId GOOGLE = new ProviderId("google");
    public static final ProviderId OLLAMA = new ProviderId("ollama");

    @Override
    public String toString() {
        return value;
    }
}
