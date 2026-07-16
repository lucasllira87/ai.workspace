package com.aiworkspace.iam.application.service;

import com.aiworkspace.iam.application.command.LoginCommand;
import com.aiworkspace.iam.application.dto.AuthTokenDto;
import com.aiworkspace.iam.application.mapper.UserDtoMapper;
import com.aiworkspace.iam.application.port.in.LoginUseCase;
import com.aiworkspace.iam.application.port.out.TokenGeneratorPort;
import com.aiworkspace.iam.application.port.out.TokenPolicyPort;
import com.aiworkspace.iam.domain.entity.RefreshToken;
import com.aiworkspace.iam.domain.entity.User;
import com.aiworkspace.iam.domain.repository.RefreshTokenRepository;
import com.aiworkspace.iam.domain.repository.UserRepository;
import com.aiworkspace.iam.domain.service.PasswordHasher;
import com.aiworkspace.iam.domain.valueobject.Email;
import com.aiworkspace.iam.domain.valueobject.HashedPassword;
import com.aiworkspace.shared.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class LoginService implements LoginUseCase {

    // Dummy hash used when user is not found to prevent timing-based email enumeration.
    // BCrypt always runs ~250ms; without this, not-found path returns in <1ms.
    private static final String DUMMY_HASH =
            "$2a$12$kZHhE8j3P4tGYqHd6J7b.OdSTwjLaqEbRSfJ4P2fkFG1k1j4OxTWm";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final TokenGeneratorPort tokenGenerator;
    private final TokenPolicyPort tokenPolicy;

    public LoginService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                         PasswordHasher passwordHasher, TokenGeneratorPort tokenGenerator,
                         TokenPolicyPort tokenPolicy) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
        this.tokenGenerator = tokenGenerator;
        this.tokenPolicy = tokenPolicy;
    }

    @Override
    public AuthTokenDto execute(LoginCommand command) {
        Optional<User> userOpt = userRepository.findByEmail(new Email(command.email()));

        if (userOpt.isEmpty()) {
            // Run BCrypt anyway to prevent timing attacks — response time must be identical
            passwordHasher.matches(command.password(), HashedPassword.ofHash(DUMMY_HASH));
            throw new UnauthorizedException("Invalid credentials");
        }

        User user = userOpt.get();

        if (!passwordHasher.matches(command.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is not active");
        }

        String accessToken = tokenGenerator.generateAccessToken(user);
        String rawRefreshToken = tokenGenerator.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.create(
                user.getId(),
                rawRefreshToken,
                command.deviceName(),
                command.deviceType(),
                command.ipAddress(),
                tokenPolicy.refreshTokenExpirationSeconds()
        );

        refreshTokenRepository.save(refreshToken);

        return new AuthTokenDto(accessToken, rawRefreshToken, UserDtoMapper.toDto(user));
    }
}
