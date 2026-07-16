package com.aiworkspace.shared.presentation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        String message,
        List<FieldError> errors,
        String path,
        Instant timestamp
) {
    public record FieldError(String field, String message) {}

    public static ErrorResponse of(String message, String path) {
        return new ErrorResponse(false, message, null, path, Instant.now());
    }

    public static ErrorResponse ofValidation(String message, List<FieldError> errors, String path) {
        return new ErrorResponse(false, message, errors, path, Instant.now());
    }
}
