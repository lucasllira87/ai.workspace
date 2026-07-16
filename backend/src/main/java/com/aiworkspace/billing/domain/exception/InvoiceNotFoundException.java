package com.aiworkspace.billing.domain.exception;

import com.aiworkspace.shared.exception.NotFoundException;

import java.util.UUID;

public class InvoiceNotFoundException extends NotFoundException {
    public InvoiceNotFoundException(UUID id) {
        super("Invoice not found: " + id);
    }
}
