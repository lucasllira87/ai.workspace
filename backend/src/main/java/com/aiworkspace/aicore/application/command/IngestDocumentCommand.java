package com.aiworkspace.aicore.application.command;

import java.util.UUID;

public record IngestDocumentCommand(
        UUID documentId,
        String fileName,
        String mimeType,
        byte[] content,
        String module,
        String requestedBy
) {}
