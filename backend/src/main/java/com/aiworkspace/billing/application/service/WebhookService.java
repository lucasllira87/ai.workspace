package com.aiworkspace.billing.application.service;

import com.aiworkspace.billing.application.command.HandleWebhookCommand;
import com.aiworkspace.billing.application.port.in.HandlePaymentWebhookUseCase;
import com.aiworkspace.billing.application.port.out.BillingNotificationPort;
import com.aiworkspace.billing.application.port.out.InvoiceRepository;
import com.aiworkspace.billing.application.port.out.PaymentGatewayPort;
import com.aiworkspace.billing.application.port.out.SubscriptionRepository;
import com.aiworkspace.billing.domain.model.Invoice;
import com.aiworkspace.billing.domain.model.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WebhookService implements HandlePaymentWebhookUseCase {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final PaymentGatewayPort paymentGateway;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BillingNotificationPort notificationPort;
    private final ApplicationEventPublisher eventPublisher;

    public WebhookService(PaymentGatewayPort paymentGateway,
                           InvoiceRepository invoiceRepository,
                           SubscriptionRepository subscriptionRepository,
                           BillingNotificationPort notificationPort,
                           ApplicationEventPublisher eventPublisher) {
        this.paymentGateway = paymentGateway;
        this.invoiceRepository = invoiceRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.notificationPort = notificationPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void handle(HandleWebhookCommand command) {
        PaymentGatewayPort.WebhookEvent event =
                paymentGateway.parseAndVerify(command.payload(), command.signature());

        switch (event.type()) {
            case "invoice.payment_succeeded" -> handlePaymentSucceeded(event);
            case "invoice.payment_failed" -> handlePaymentFailed(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            default -> log.debug("Unhandled webhook event type: {}", event.type());
        }
    }

    private void handlePaymentSucceeded(PaymentGatewayPort.WebhookEvent event) {
        if (event.externalInvoiceId() == null) return;

        // Idempotent — if already PAID, no-op
        Optional<Invoice> invoiceOpt = invoiceRepository.findByExternalId(event.externalInvoiceId());
        if (invoiceOpt.isEmpty()) {
            log.warn("Received payment_succeeded for unknown invoice: {}", event.externalInvoiceId());
            return;
        }

        Invoice invoice = invoiceOpt.get();
        invoice.markPaid();
        invoiceRepository.save(invoice);
        invoice.getDomainEvents().forEach(eventPublisher::publishEvent);

        // Renew subscription on successful payment
        if (event.externalSubscriptionId() != null) {
            subscriptionRepository.findByExternalId(event.externalSubscriptionId())
                    .ifPresent(sub -> {
                        sub.renew(sub.getCurrentPeriod().next());
                        subscriptionRepository.save(sub);
                        sub.getDomainEvents().forEach(eventPublisher::publishEvent);
                    });
        }
    }

    private void handlePaymentFailed(PaymentGatewayPort.WebhookEvent event) {
        if (event.externalInvoiceId() == null) return;

        invoiceRepository.findByExternalId(event.externalInvoiceId()).ifPresent(invoice -> {
            invoice.markFailed();
            invoiceRepository.save(invoice);
            invoice.getDomainEvents().forEach(eventPublisher::publishEvent);
            notificationPort.notifyPaymentFailed(invoice.getUserId(), invoice.getId());
        });

        if (event.externalSubscriptionId() != null) {
            subscriptionRepository.findByExternalId(event.externalSubscriptionId())
                    .ifPresent(sub -> {
                        sub.markPastDue();
                        subscriptionRepository.save(sub);
                    });
        }
    }

    private void handleSubscriptionDeleted(PaymentGatewayPort.WebhookEvent event) {
        if (event.externalSubscriptionId() == null) return;

        subscriptionRepository.findByExternalId(event.externalSubscriptionId())
                .ifPresent(sub -> {
                    if (!sub.isAccessible()) return; // already canceled or expired — idempotent
                    sub.cancel();
                    subscriptionRepository.save(sub);
                    sub.getDomainEvents().forEach(eventPublisher::publishEvent);
                    notificationPort.notifySubscriptionCanceled(sub.getUserId());
                });
    }
}
