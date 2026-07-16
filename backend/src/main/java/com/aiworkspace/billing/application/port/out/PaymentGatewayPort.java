package com.aiworkspace.billing.application.port.out;

import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.Money;

import java.util.UUID;

public interface PaymentGatewayPort {

    record GatewaySubscription(String externalId, BillingPeriod period) {}

    record GatewayInvoice(String externalId) {}

    record WebhookEvent(String type, String externalSubscriptionId,
                        String externalInvoiceId, boolean succeeded) {}

    GatewaySubscription createSubscription(UUID userId, String planExternalId,
                                            String paymentMethodId);

    void cancelSubscription(String externalSubscriptionId);

    GatewayInvoice createInvoice(UUID userId, String externalSubscriptionId,
                                  Money amount, String description);

    WebhookEvent parseAndVerify(String payload, String signature);
}
