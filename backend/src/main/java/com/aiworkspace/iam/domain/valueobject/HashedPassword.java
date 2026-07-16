package com.aiworkspace.iam.domain.valueobject;

import com.aiworkspace.shared.domain.ValueObject;

import java.util.Objects;

public record HashedPassword(String value) implements ValueObject {

    public HashedPassword {
        Objects.requireNonNull(value, "HashedPassword cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("HashedPassword cannot be blank");
        }
    }

    public static HashedPassword ofHash(String hash) {
        return new HashedPassword(hash);
    }
}
