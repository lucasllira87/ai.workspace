package com.aiworkspace.iam.application.port.in;

import com.aiworkspace.iam.application.command.LogoutCommand;

public interface LogoutUseCase {
    void execute(LogoutCommand command);
}
