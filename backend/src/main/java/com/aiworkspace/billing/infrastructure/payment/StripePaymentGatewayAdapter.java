package com.aiworkspace.billing.infrastructure.payment;

import com.aiworkspace.billing.application.port.out.PaymentGatewayPort;
import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.Money;
import com.aiworkspace.shared.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Adapter for Stripe payment gateway.
 * Actual Stripe SDK calls go here — the interface keeps the application layer clean.
 *
 * In production, inject com.stripe.StripeClient and call Stripe APIs.
 * For now, this adapter is a stub that documents the integration contract.
 */
@Component
public class StripePaymentGatewayAdapter implements PaymentGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGatewayAdapter.class);

    @Value("${billing.stripe.secret-key:}")
    private String secretKey;

    @Value("${billing.stripe.webhook-secret:}")
    private String webhookSecret;

    @Override
    public GatewaySubscription createSubscription(UUID userId, String planExternalId,
                                                   String paymentMethodId) {
        // TODO: Integrate Stripe SDK
        // Customer customer = stripeClient.customers().create(...)
        // Subscription sub = stripeClient.subscriptions().create(...)
        log.info("Stripe createSubscription — userId={} plan={}", userId, planExternalId);

        // Stub: return fake external ID for development
        return new GatewaySubscription("sub_" + UUID.randomUUID().toString().replace("-", ""),
                BillingPeriod.monthStartingNow());
    }

    @Override
    public void cancelSubscription(String externalSubscriptionId) {
        // TODO: stripeClient.subscriptions().cancel(externalSubscriptionId)
        log.info("Stripe cancelSubscription — externalId={}", externalSubscriptionId);
    }

    @Override
    public GatewayInvoice createInvoice(UUID userId, String externalSubscriptionId,
                                         Money amount, String description) {
        // TODO: stripeClient.invoices().create(...)
        log.info("Stripe createInvoice — userId={} amount={}", userId, amount.amount());
        return new GatewayInvoice("in_" + UUID.randomUUID().toString().replace("-", ""));
    }

    @Override
    public WebhookEvent parseAndVerify(String payload, String signature) {
        if (webhookSecret.isBlank()) {
            throw new DomainException("Stripe webhook secret is not configured");
        }
        // TODO: Webhook event = Webhook.constructEvent(payload, signature, webhookSecret)
        // Return WebhookEvent mapped from Stripe event object
        throw new DomainException("Stripe webhook verification not yet implemented");
    }
}
