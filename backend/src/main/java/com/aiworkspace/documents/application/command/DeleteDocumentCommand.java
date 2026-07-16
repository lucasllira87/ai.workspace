package com.aiworkspace.documents.application.command;

import java.util.UUID;

public record DeleteDocumentCommand(
        UUID documentId,
        UUID ownerId
) {}
