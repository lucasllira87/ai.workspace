package com.aiworkspace.iam.infrastructure.security;

import com.aiworkspace.iam.domain.service.PasswordHasher;
import com.aiworkspace.iam.domain.valueobject.HashedPassword;
import com.aiworkspace.shared.exception.DomainException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasher {

    private static final int BCRYPT_STRENGTH = 12;
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);

    @Override
    public HashedPassword hash(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new DomainException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        return HashedPassword.ofHash(encoder.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, HashedPassword hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return encoder.matches(rawPassword, hashedPassword.value());
    }
}
