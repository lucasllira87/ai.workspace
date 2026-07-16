package com.aiworkspace.billing.application.dto;

import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.Invoice;
import com.aiworkspace.billing.domain.model.InvoiceLineItem;
import com.aiworkspace.billing.domain.model.InvoiceStatus;
import com.aiworkspace.billing.domain.model.Money;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InvoiceDto(UUID id, UUID userId, UUID subscriptionId, BillingPeriod period,
                          List<InvoiceLineItem> lineItems, Money totalAmount,
                          InvoiceStatus status, Instant issuedAt, Instant paidAt) {
    public static InvoiceDto from(Invoice invoice) {
        return new InvoiceDto(invoice.getId(), invoice.getUserId(), invoice.getSubscriptionId(),
                invoice.getPeriod(), invoice.getLineItems(), invoice.getTotalAmount(),
                invoice.getStatus(), invoice.getIssuedAt(), invoice.getPaidAt());
    }
}
