package com.aiworkspace.billing.application.port.in;

import com.aiworkspace.billing.application.command.RecordUsageCommand;

public interface RecordUsageUseCase {
    void record(RecordUsageCommand command);
}
