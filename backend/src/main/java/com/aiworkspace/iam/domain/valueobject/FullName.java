package com.aiworkspace.iam.domain.valueobject;

import com.aiworkspace.shared.domain.ValueObject;
import com.aiworkspace.shared.exception.DomainException;

import java.util.Objects;

public record FullName(String value) implements ValueObject {

    public FullName {
        Objects.requireNonNull(value, "FullName cannot be null");
        value = value.trim();
        if (value.isBlank()) {
            throw new DomainException("Full name cannot be blank");
        }
        if (value.length() > 100) {
            throw new DomainException("Full name cannot exceed 100 characters");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
