package com.aiworkspace.billing.application.port.in;

import com.aiworkspace.billing.application.dto.InvoiceDto;

import java.util.List;
import java.util.UUID;

public interface GetInvoiceUseCase {
    InvoiceDto getById(UUID invoiceId, UUID userId);

    List<InvoiceDto> listByUser(UUID userId);
}
