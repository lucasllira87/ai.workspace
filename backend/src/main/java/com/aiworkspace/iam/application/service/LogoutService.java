package com.aiworkspace.iam.application.service;

import com.aiworkspace.iam.application.command.LogoutCommand;
import com.aiworkspace.iam.application.port.in.LogoutUseCase;
import com.aiworkspace.iam.domain.entity.RefreshToken;
import com.aiworkspace.iam.domain.repository.RefreshTokenRepository;
import com.aiworkspace.iam.domain.valueobject.UserId;
import com.aiworkspace.shared.exception.ForbiddenException;
import com.aiworkspace.shared.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void execute(LogoutCommand command) {
        RefreshToken token = refreshTokenRepository.findByToken(command.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        // Verify the token belongs to the authenticated user to prevent unauthorized revocation
        UserId authenticatedUser = UserId.of(command.authenticatedUserId());
        if (!token.getUserId().equals(authenticatedUser)) {
            throw new ForbiddenException("Cannot revoke another user's session");
        }

        token.revoke();
        refreshTokenRepository.save(token);
    }
}
