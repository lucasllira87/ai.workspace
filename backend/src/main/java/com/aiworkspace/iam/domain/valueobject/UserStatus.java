package com.aiworkspace.iam.domain.valueobject;

public enum UserStatus {
    ACTIVE, BLOCKED, PENDING_VERIFICATION;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
