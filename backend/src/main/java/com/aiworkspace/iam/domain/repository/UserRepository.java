package com.aiworkspace.iam.domain.repository;

import com.aiworkspace.iam.domain.entity.User;
import com.aiworkspace.iam.domain.valueobject.Email;
import com.aiworkspace.iam.domain.valueobject.UserId;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(Email email);
    boolean existsByEmail(Email email);
}
