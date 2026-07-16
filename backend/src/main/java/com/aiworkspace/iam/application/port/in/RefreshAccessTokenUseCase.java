package com.aiworkspace.iam.application.port.in;

import com.aiworkspace.iam.application.command.RefreshTokenCommand;
import com.aiworkspace.iam.application.dto.AuthTokenDto;

public interface RefreshAccessTokenUseCase {
    AuthTokenDto execute(RefreshTokenCommand command);
}
