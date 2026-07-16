package com.aiworkspace.billing.application.port.out;

import com.aiworkspace.billing.domain.model.Invoice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);

    Optional<Invoice> findById(UUID id);

    Optional<Invoice> findByExternalId(String externalId);

    List<Invoice> findAllByUserId(UUID userId);
}
