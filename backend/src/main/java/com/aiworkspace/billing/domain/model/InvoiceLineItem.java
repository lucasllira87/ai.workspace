package com.aiworkspace.billing.domain.model;

import java.util.UUID;

public record InvoiceLineItem(UUID id, String description, Money amount, int quantity) {

    public static InvoiceLineItem of(String description, Money amount) {
        return new InvoiceLineItem(UUID.randomUUID(), description, amount, 1);
    }

    public Money total() {
        return amount.multiply(quantity);
    }
}
