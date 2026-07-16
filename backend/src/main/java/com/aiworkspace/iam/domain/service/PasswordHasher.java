package com.aiworkspace.iam.domain.service;

import com.aiworkspace.iam.domain.valueobject.HashedPassword;

public interface PasswordHasher {
    HashedPassword hash(String rawPassword);
    boolean matches(String rawPassword, HashedPassword hashedPassword);
}
