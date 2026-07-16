package com.aiworkspace.billing.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record UpgradeSubscriptionRequest(
        @NotBlank String planName,
        @NotBlank String paymentMethodId
) {}
