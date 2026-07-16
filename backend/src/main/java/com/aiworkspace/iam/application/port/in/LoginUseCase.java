package com.aiworkspace.iam.application.port.in;

import com.aiworkspace.iam.application.command.LoginCommand;
import com.aiworkspace.iam.application.dto.AuthTokenDto;

public interface LoginUseCase {
    AuthTokenDto execute(LoginCommand command);
}
