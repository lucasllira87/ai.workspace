package com.aiworkspace.iam.domain.entity;

import com.aiworkspace.iam.domain.event.UserRegisteredEvent;
import com.aiworkspace.iam.domain.valueobject.Email;
import com.aiworkspace.iam.domain.valueobject.FullName;
import com.aiworkspace.iam.domain.valueobject.HashedPassword;
import com.aiworkspace.iam.domain.valueobject.Role;
import com.aiworkspace.iam.domain.valueobject.UserId;
import com.aiworkspace.iam.domain.valueobject.UserStatus;
import com.aiworkspace.shared.domain.AggregateRoot;
import com.aiworkspace.shared.exception.DomainException;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class User extends AggregateRoot {

    private UserId id;
    private Email email;
    private HashedPassword password;
    private FullName fullName;
    private UserStatus status;
    private Set<Role> roles;
    private UserProfile profile;
    private Instant createdAt;
    private Instant updatedAt;

    private User() {}

    public static User create(Email email, HashedPassword password, FullName fullName) {
        User user = new User();
        user.id = UserId.generate();
        user.email = email;
        user.password = password;
        user.fullName = fullName;
        user.status = UserStatus.ACTIVE;
        user.roles = new HashSet<>(Set.of(Role.USER));
        user.profile = UserProfile.createDefault();
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        user.registerEvent(new UserRegisteredEvent(user.id, user.email));
        return user;
    }

    public static User reconstitute(UserId id, Email email, HashedPassword password,
                                     FullName fullName, UserStatus status, Set<Role> roles,
                                     UserProfile profile, Instant createdAt, Instant updatedAt) {
        User user = new User();
        user.id = id;
        user.email = email;
        user.password = password;
        user.fullName = fullName;
        user.status = status;
        user.roles = new HashSet<>(roles);
        user.profile = profile;
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        return user;
    }

    public void changePassword(HashedPassword newPassword) {
        if (newPassword.value().equals(this.password.value())) {
            throw new DomainException("New password must differ from the current password");
        }
        this.password = newPassword;
        this.updatedAt = Instant.now();
    }

    public void block() {
        if (this.status == UserStatus.BLOCKED) {
            throw new DomainException("User is already blocked");
        }
        this.status = UserStatus.BLOCKED;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status.isActive();
    }

    public UserId getId() { return id; }
    public Email getEmail() { return email; }
    public HashedPassword getPassword() { return password; }
    public FullName getFullName() { return fullName; }
    public UserStatus getStatus() { return status; }
    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }
    public UserProfile getProfile() { return profile; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
