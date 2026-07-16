package com.aiworkspace.audit.application.port.in;

import com.aiworkspace.audit.application.command.RecordAuditEventCommand;

public interface RecordAuditEventUseCase {
    void record(RecordAuditEventCommand command);
}
