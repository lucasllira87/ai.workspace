package com.aiworkspace.iam.domain.event;

import com.aiworkspace.iam.domain.valueobject.Email;
import com.aiworkspace.iam.domain.valueobject.UserId;
import com.aiworkspace.shared.domain.DomainEvent;

import java.time.Instant;

public record UserRegisteredEvent(
        UserId userId,
        Email email,
        Instant occurredAt
) implements DomainEvent {

    public UserRegisteredEvent(UserId userId, Email email) {
        this(userId, email, Instant.now());
    }
}
