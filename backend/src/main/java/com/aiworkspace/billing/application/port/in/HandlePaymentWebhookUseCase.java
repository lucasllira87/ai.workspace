package com.aiworkspace.billing.application.port.in;

import com.aiworkspace.billing.application.command.HandleWebhookCommand;

public interface HandlePaymentWebhookUseCase {
    void handle(HandleWebhookCommand command);
}
