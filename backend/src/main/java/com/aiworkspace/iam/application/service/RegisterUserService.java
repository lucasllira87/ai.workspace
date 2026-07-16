package com.aiworkspace.iam.application.service;

import com.aiworkspace.iam.application.command.RegisterUserCommand;
import com.aiworkspace.iam.application.dto.UserDto;
import com.aiworkspace.iam.application.port.in.RegisterUserUseCase;
import com.aiworkspace.iam.domain.entity.User;
import com.aiworkspace.iam.domain.repository.UserRepository;
import com.aiworkspace.iam.domain.service.PasswordHasher;
import com.aiworkspace.iam.domain.valueobject.Email;
import com.aiworkspace.iam.domain.valueobject.FullName;
import com.aiworkspace.shared.exception.ConflictException;
import com.aiworkspace.iam.application.mapper.UserDtoMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final ApplicationEventPublisher eventPublisher;

    public RegisterUserService(UserRepository userRepository, PasswordHasher passwordHasher,
                                ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserDto execute(RegisterUserCommand command) {
        Email email = new Email(command.email());

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }

        User user = User.create(
                email,
                passwordHasher.hash(command.password()),
                new FullName(command.fullName())
        );

        User saved = userRepository.save(user);

        user.getDomainEvents().forEach(eventPublisher::publishEvent);
        user.clearDomainEvents();

        return UserDtoMapper.toDto(saved);
    }
}
