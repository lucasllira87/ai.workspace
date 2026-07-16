package com.aiworkspace.iam.application;

import com.aiworkspace.iam.application.command.RegisterUserCommand;
import com.aiworkspace.iam.application.dto.UserDto;
import com.aiworkspace.iam.application.service.RegisterUserService;
import com.aiworkspace.iam.domain.entity.User;
import com.aiworkspace.iam.domain.repository.UserRepository;
import com.aiworkspace.iam.domain.service.PasswordHasher;
import com.aiworkspace.iam.domain.valueobject.Email;
import com.aiworkspace.iam.domain.valueobject.FullName;
import com.aiworkspace.iam.domain.valueobject.HashedPassword;
import com.aiworkspace.shared.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordHasher passwordHasher;
    @Mock ApplicationEventPublisher eventPublisher;

    RegisterUserService service;

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(userRepository, passwordHasher, eventPublisher);
    }

    @Test
    void execute_createsUserAndPublishesEvent() {
        RegisterUserCommand command = new RegisterUserCommand("john@example.com", "Secret123!", "John Doe");
        HashedPassword hash = HashedPassword.ofHash("$2a$12$hashed");
        when(passwordHasher.hash("Secret123!")).thenReturn(hash);
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = service.execute(command);

        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.fullName()).isEqualTo("John Doe");

        verify(userRepository).save(any(User.class));
        verify(eventPublisher, atLeastOnce()).publishEvent(any());
    }

    @Test
    void execute_hashesPasswordBeforeStoring() {
        RegisterUserCommand command = new RegisterUserCommand("john@example.com", "Secret123!", "John Doe");
        HashedPassword hash = HashedPassword.ofHash("$2a$12$hashed");
        when(passwordHasher.hash("Secret123!")).thenReturn(hash);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.execute(command);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo(hash);
    }

    @Test
    void execute_throwsConflictWhenEmailAlreadyRegistered() {
        RegisterUserCommand command = new RegisterUserCommand("existing@example.com", "pass", "Name");
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void execute_normalizeseEmailToLowerCase() {
        RegisterUserCommand command = new RegisterUserCommand("John@EXAMPLE.COM", "pass", "Name");
        when(passwordHasher.hash(any())).thenReturn(HashedPassword.ofHash("$2a$12$h"));
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = service.execute(command);

        assertThat(result.email()).isEqualTo("john@example.com");
    }
}
