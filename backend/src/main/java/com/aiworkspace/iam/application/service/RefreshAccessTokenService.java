package com.aiworkspace.iam.application.service;

import com.aiworkspace.iam.application.command.RefreshTokenCommand;
import com.aiworkspace.iam.application.dto.AuthTokenDto;
import com.aiworkspace.iam.application.mapper.UserDtoMapper;
import com.aiworkspace.iam.application.port.in.RefreshAccessTokenUseCase;
import com.aiworkspace.iam.application.port.out.TokenGeneratorPort;
import com.aiworkspace.iam.application.port.out.TokenPolicyPort;
import com.aiworkspace.iam.domain.entity.RefreshToken;
import com.aiworkspace.iam.domain.entity.User;
import com.aiworkspace.iam.domain.repository.RefreshTokenRepository;
import com.aiworkspace.iam.domain.repository.UserRepository;
import com.aiworkspace.shared.exception.NotFoundException;
import com.aiworkspace.shared.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RefreshAccessTokenService implements RefreshAccessTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenGeneratorPort tokenGenerator;
    private final TokenPolicyPort tokenPolicy;

    public RefreshAccessTokenService(RefreshTokenRepository refreshTokenRepository,
                                      UserRepository userRepository,
                                      TokenGeneratorPort tokenGenerator,
                                      TokenPolicyPort tokenPolicy) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenGenerator = tokenGenerator;
        this.tokenPolicy = tokenPolicy;
    }

    @Override
    public AuthTokenDto execute(RefreshTokenCommand command) {
        RefreshToken existing = refreshTokenRepository.findByToken(command.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!existing.isValid()) {
            throw new UnauthorizedException("Refresh token has expired or been revoked");
        }

        User user = userRepository.findById(existing.getUserId())
                .orElseThrow(() -> NotFoundException.of("User", existing.getUserId()));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is not active");
        }

        String newRawToken = tokenGenerator.generateRefreshToken();
        RefreshToken rotated = existing.rotate(newRawToken, tokenPolicy.refreshTokenExpirationSeconds());

        refreshTokenRepository.save(existing);
        RefreshToken saved = refreshTokenRepository.save(rotated);

        String accessToken = tokenGenerator.generateAccessToken(user);

        return new AuthTokenDto(accessToken, saved.getToken(), UserDtoMapper.toDto(user));
    }
}
