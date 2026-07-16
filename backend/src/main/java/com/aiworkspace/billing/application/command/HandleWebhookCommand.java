package com.aiworkspace.billing.application.command;

public record HandleWebhookCommand(String payload, String signature) {}
