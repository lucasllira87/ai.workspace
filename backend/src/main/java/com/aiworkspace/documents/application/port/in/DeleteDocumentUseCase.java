package com.aiworkspace.documents.application.port.in;

import com.aiworkspace.documents.application.command.DeleteDocumentCommand;

public interface DeleteDocumentUseCase {
    void delete(DeleteDocumentCommand command);
}
