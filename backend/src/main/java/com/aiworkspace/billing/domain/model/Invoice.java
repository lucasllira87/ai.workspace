package com.aiworkspace.billing.domain.model;

import com.aiworkspace.billing.domain.event.InvoiceGeneratedEvent;
import com.aiworkspace.billing.domain.event.PaymentFailedEvent;
import com.aiworkspace.billing.domain.event.PaymentSucceededEvent;
import com.aiworkspace.shared.domain.AggregateRoot;
import com.aiworkspace.shared.exception.DomainException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Invoice extends AggregateRoot {

    private final UUID id;
    private final UUID userId;
    private final UUID subscriptionId;
    private final BillingPeriod period;
    private final List<InvoiceLineItem> lineItems;
    private Money totalAmount;
    private InvoiceStatus status;
    private String externalId;
    private Instant issuedAt;
    private Instant paidAt;
    private final Instant createdAt;

    private Invoice(UUID id, UUID userId, UUID subscriptionId, BillingPeriod period,
                    List<InvoiceLineItem> lineItems, Money totalAmount, InvoiceStatus status,
                    String externalId, Instant issuedAt, Instant paidAt, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.period = period;
        this.lineItems = new ArrayList<>(lineItems);
        this.totalAmount = totalAmount;
        this.status = status;
        this.externalId = externalId;
        this.issuedAt = issuedAt;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
    }

    public static Invoice generate(UUID id, UUID userId, UUID subscriptionId,
                                    BillingPeriod period, List<InvoiceLineItem> lineItems,
                                    String currency) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(lineItems, "lineItems");
        if (lineItems.isEmpty()) {
            throw new DomainException("Invoice must have at least one line item");
        }

        Money total = lineItems.stream()
                .map(InvoiceLineItem::total)
                .reduce(Money.zero(currency), Money::add);

        Instant now = Instant.now();
        Invoice invoice = new Invoice(id, userId, subscriptionId, period,
                lineItems, total, InvoiceStatus.DRAFT, null, null, null, now);
        invoice.registerEvent(new InvoiceGeneratedEvent(id, userId, subscriptionId, total, period, now));
        return invoice;
    }

    public static Invoice reconstitute(UUID id, UUID userId, UUID subscriptionId,
                                        BillingPeriod period, List<InvoiceLineItem> lineItems,
                                        Money totalAmount, InvoiceStatus status, String externalId,
                                        Instant issuedAt, Instant paidAt, Instant createdAt) {
        return new Invoice(id, userId, subscriptionId, period, lineItems,
                totalAmount, status, externalId, issuedAt, paidAt, createdAt);
    }

    public void issue(String externalId) {
        if (status != InvoiceStatus.DRAFT) {
            throw new DomainException("Only DRAFT invoices can be issued");
        }
        this.externalId = externalId;
        this.status = InvoiceStatus.ISSUED;
        this.issuedAt = Instant.now();
    }

    public void markPaid() {
        if (status == InvoiceStatus.PAID) return;
        if (status != InvoiceStatus.ISSUED && status != InvoiceStatus.DRAFT) {
            throw new DomainException("Cannot mark invoice as paid — current status: " + status);
        }
        this.status = InvoiceStatus.PAID;
        this.paidAt = Instant.now();
        registerEvent(new PaymentSucceededEvent(id, userId, subscriptionId, totalAmount, paidAt));
    }

    public void markFailed() {
        if (status != InvoiceStatus.ISSUED) {
            throw new DomainException("Cannot mark payment as failed — current status: " + status);
        }
        this.status = InvoiceStatus.PAYMENT_FAILED;
        registerEvent(new PaymentFailedEvent(id, userId, subscriptionId, totalAmount, Instant.now()));
    }

    public void voidInvoice() {
        if (status == InvoiceStatus.PAID) {
            throw new DomainException("Cannot void a paid invoice");
        }
        this.status = InvoiceStatus.VOID;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getSubscriptionId() { return subscriptionId; }
    public BillingPeriod getPeriod() { return period; }
    public List<InvoiceLineItem> getLineItems() { return Collections.unmodifiableList(lineItems); }
    public Money getTotalAmount() { return totalAmount; }
    public InvoiceStatus getStatus() { return status; }
    public String getExternalId() { return externalId; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getCreatedAt() { return createdAt; }
}
