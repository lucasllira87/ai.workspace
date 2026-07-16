package com.aiworkspace.documents.application.port.in;

import com.aiworkspace.documents.application.command.UploadDocumentCommand;
import com.aiworkspace.documents.application.dto.DocumentDto;

public interface UploadDocumentUseCase {
    DocumentDto upload(UploadDocumentCommand command);
}
