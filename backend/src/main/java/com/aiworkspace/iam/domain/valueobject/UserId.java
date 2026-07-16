package com.aiworkspace.iam.domain.valueobject;

import com.aiworkspace.shared.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) implements ValueObject {

    public UserId {
        Objects.requireNonNull(value, "UserId cannot be null");
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(String value) {
        return new UserId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
