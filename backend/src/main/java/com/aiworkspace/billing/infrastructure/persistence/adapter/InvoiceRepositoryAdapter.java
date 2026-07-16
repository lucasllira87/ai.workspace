package com.aiworkspace.billing.infrastructure.persistence.adapter;

import com.aiworkspace.billing.application.port.out.InvoiceRepository;
import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.Invoice;
import com.aiworkspace.billing.domain.model.InvoiceLineItem;
import com.aiworkspace.billing.domain.model.InvoiceStatus;
import com.aiworkspace.billing.domain.model.Money;
import com.aiworkspace.billing.infrastructure.persistence.entity.InvoiceJpaEntity;
import com.aiworkspace.billing.infrastructure.persistence.entity.InvoiceLineItemJpaEntity;
import com.aiworkspace.billing.infrastructure.persistence.repository.InvoiceJpaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final InvoiceJpaRepository jpaRepository;

    public InvoiceRepositoryAdapter(InvoiceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Invoice save(Invoice invoice) {
        jpaRepository.save(toEntity(invoice));
        return invoice;
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Invoice> findByExternalId(String externalId) {
        return jpaRepository.findByExternalId(externalId).map(this::toDomain);
    }

    @Override
    public List<Invoice> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDomain).toList();
    }

    private InvoiceJpaEntity toEntity(Invoice invoice) {
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.setId(invoice.getId());
        entity.setUserId(invoice.getUserId());
        entity.setSubscriptionId(invoice.getSubscriptionId());
        entity.setPeriodStart(invoice.getPeriod().startAt());
        entity.setPeriodEnd(invoice.getPeriod().endAt());
        entity.setTotalAmount(invoice.getTotalAmount().amount());
        entity.setCurrency(invoice.getTotalAmount().currency());
        entity.setStatus(invoice.getStatus().name());
        entity.setExternalId(invoice.getExternalId());
        entity.setIssuedAt(invoice.getIssuedAt());
        entity.setPaidAt(invoice.getPaidAt());
        entity.setCreatedAt(invoice.getCreatedAt());

        List<InvoiceLineItemJpaEntity> itemEntities = new ArrayList<>();
        for (InvoiceLineItem item : invoice.getLineItems()) {
            InvoiceLineItemJpaEntity ie = new InvoiceLineItemJpaEntity();
            ie.setId(item.id());
            ie.setInvoice(entity);
            ie.setDescription(item.description());
            ie.setAmount(item.amount().amount());
            ie.setCurrency(item.amount().currency());
            ie.setQuantity(item.quantity());
            itemEntities.add(ie);
        }
        entity.setLineItems(itemEntities);
        return entity;
    }

    private Invoice toDomain(InvoiceJpaEntity entity) {
        List<InvoiceLineItem> lineItems = entity.getLineItems().stream()
                .map(ie -> new InvoiceLineItem(ie.getId(), ie.getDescription(),
                        Money.of(ie.getAmount(), ie.getCurrency()), ie.getQuantity()))
                .toList();

        return Invoice.reconstitute(
                entity.getId(), entity.getUserId(), entity.getSubscriptionId(),
                new BillingPeriod(entity.getPeriodStart(), entity.getPeriodEnd()),
                lineItems,
                Money.of(entity.getTotalAmount(), entity.getCurrency()),
                InvoiceStatus.valueOf(entity.getStatus()),
                entity.getExternalId(),
                entity.getIssuedAt(), entity.getPaidAt(), entity.getCreatedAt()
        );
    }
}
