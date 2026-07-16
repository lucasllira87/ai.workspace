package com.aiworkspace.iam.application.port.in;

import com.aiworkspace.iam.application.command.RegisterUserCommand;
import com.aiworkspace.iam.application.dto.UserDto;

public interface RegisterUserUseCase {
    UserDto execute(RegisterUserCommand command);
}
