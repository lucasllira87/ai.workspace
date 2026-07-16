package com.aiworkspace.billing.presentation.controller;

import com.aiworkspace.billing.application.command.HandleWebhookCommand;
import com.aiworkspace.billing.application.port.in.HandlePaymentWebhookUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/billing/webhooks")
public class StripeWebhookController {

    private final HandlePaymentWebhookUseCase handleWebhook;

    public StripeWebhookController(HandlePaymentWebhookUseCase handleWebhook) {
        this.handleWebhook = handleWebhook;
    }

    // Endpoint is public — authentication is via Stripe signature verification
    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripe(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        handleWebhook.handle(new HandleWebhookCommand(payload, signature));
        return ResponseEntity.ok().build();
    }
}
