package com.aiworkspace.iam.application.port.out;

public interface EmailPort {
    void sendPasswordResetEmail(String to, String resetLink);
    void sendWelcomeEmail(String to, String userName);
}
