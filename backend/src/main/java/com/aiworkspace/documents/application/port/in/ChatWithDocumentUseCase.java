package com.aiworkspace.documents.application.port.in;

import com.aiworkspace.documents.application.command.ChatWithDocumentCommand;
import com.aiworkspace.documents.application.dto.ChatWithDocumentDto;

public interface ChatWithDocumentUseCase {
    ChatWithDocumentDto chat(ChatWithDocumentCommand command);
}
