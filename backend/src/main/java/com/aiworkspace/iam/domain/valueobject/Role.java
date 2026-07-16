package com.aiworkspace.iam.domain.valueobject;

public enum Role {
    USER, ADMIN;

    public String toGrantedAuthority() {
        return "ROLE_" + name();
    }
}
