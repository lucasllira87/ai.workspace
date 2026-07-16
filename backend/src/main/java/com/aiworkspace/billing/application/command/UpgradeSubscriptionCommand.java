package com.aiworkspace.billing.application.command;

import java.util.UUID;

public record UpgradeSubscriptionCommand(UUID userId, String planName, String paymentMethodId) {}
