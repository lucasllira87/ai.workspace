package com.aiworkspace.documents.application.command;

import java.util.UUID;

public record UploadDocumentCommand(
        UUID ownerId,
        String title,
        String fileName,
        String mimeType,
        byte[] content
) {}
