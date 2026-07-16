package com.aiworkspace.iam.application;

import com.aiworkspace.iam.application.command.LoginCommand;
import com.aiworkspace.iam.application.dto.AuthTokenDto;
import com.aiworkspace.iam.application.service.LoginService;
import com.aiworkspace.iam.application.port.out.TokenGeneratorPort;
import com.aiworkspace.iam.application.port.out.TokenPolicyPort;
import com.aiworkspace.iam.domain.entity.User;
import com.aiworkspace.iam.domain.repository.RefreshTokenRepository;
import com.aiworkspace.iam.domain.repository.UserRepository;
import com.aiworkspace.iam.domain.service.PasswordHasher;
import com.aiworkspace.iam.domain.valueobject.Email;
import com.aiworkspace.iam.domain.valueobject.FullName;
import com.aiworkspace.iam.domain.valueobject.HashedPassword;
import com.aiworkspace.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordHasher passwordHasher;
    @Mock TokenGeneratorPort tokenGenerator;
    @Mock TokenPolicyPort tokenPolicy;

    LoginService service;

    @BeforeEach
    void setUp() {
        service = new LoginService(userRepository, refreshTokenRepository,
                passwordHasher, tokenGenerator, tokenPolicy);
    }

    @Test
    void execute_returnsTokensOnValidCredentials() {
        User user = User.create(new Email("john@example.com"),
                HashedPassword.ofHash("$2a$12$hash"), new FullName("John Doe"));
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordHasher.matches(anyString(), any())).thenReturn(true);
        when(tokenGenerator.generateAccessToken(user)).thenReturn("access-token");
        when(tokenGenerator.generateRefreshToken()).thenReturn("refresh-token");
        when(tokenPolicy.refreshTokenExpirationSeconds()).thenReturn(604800L);

        AuthTokenDto result = service.execute(loginCommand());

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.user().email()).isEqualTo("john@example.com");
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void execute_throwsUnauthorizedWhenUserNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        // Even when user not found, BCrypt dummy hash still runs (timing attack prevention)
        when(passwordHasher.matches(anyString(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.execute(loginCommand()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid credentials");

        verify(tokenGenerator, never()).generateAccessToken(any());
    }

    @Test
    void execute_throwsUnauthorizedOnWrongPassword() {
        User user = User.create(new Email("john@example.com"),
                HashedPassword.ofHash("$2a$12$hash"), new FullName("John Doe"));
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordHasher.matches(anyString(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.execute(loginCommand()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void execute_throwsUnauthorizedWhenUserBlocked() {
        User user = User.create(new Email("john@example.com"),
                HashedPassword.ofHash("$2a$12$hash"), new FullName("John Doe"));
        user.block();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordHasher.matches(anyString(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.execute(loginCommand()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void execute_doesNotRevealWhetherEmailExists_timingAttack() {
        // Both paths (user not found vs wrong password) should call passwordHasher.matches()
        // to prevent timing-based email enumeration
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordHasher.matches(anyString(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.execute(loginCommand()))
                .isInstanceOf(UnauthorizedException.class);

        // Verify BCrypt ran even though user was not found
        verify(passwordHasher).matches(anyString(), any(HashedPassword.class));
    }

    private LoginCommand loginCommand() {
        return new LoginCommand("john@example.com", "Secret123!", "Chrome", "WEB", "127.0.0.1");
    }
}
