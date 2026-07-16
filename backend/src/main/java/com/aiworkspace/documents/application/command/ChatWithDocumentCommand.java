package com.aiworkspace.documents.application.command;

import java.util.UUID;

public record ChatWithDocumentCommand(
        UUID documentId,
        UUID ownerId,
        String question
) {}
