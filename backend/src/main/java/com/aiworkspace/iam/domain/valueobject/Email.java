package com.aiworkspace.iam.domain.valueobject;

import com.aiworkspace.shared.domain.ValueObject;
import com.aiworkspace.shared.exception.DomainException;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) implements ValueObject {

    private static final Pattern PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    public Email {
        Objects.requireNonNull(value, "Email cannot be null");
        String normalized = value.trim().toLowerCase();
        if (!PATTERN.matcher(normalized).matches()) {
            throw new DomainException("Invalid email format");
        }
        value = normalized;
    }

    @Override
    public String toString() {
        return value;
    }
}
