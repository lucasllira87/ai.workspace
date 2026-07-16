package com.aiworkspace.aicore.application.port.in;

import com.aiworkspace.aicore.application.command.IngestDocumentCommand;
import com.aiworkspace.aicore.application.dto.IngestionResultDto;

public interface IngestDocumentUseCase {

    IngestionResultDto execute(IngestDocumentCommand command);
}
