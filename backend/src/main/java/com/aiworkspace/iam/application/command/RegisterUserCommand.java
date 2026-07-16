package com.aiworkspace.iam.application.command;

public record RegisterUserCommand(String email, String password, String fullName) {}
