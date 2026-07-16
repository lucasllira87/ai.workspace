package com.aiworkspace.billing.infrastructure.persistence.repository;

import com.aiworkspace.billing.infrastructure.persistence.entity.InvoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceJpaEntity, UUID> {

    Optional<InvoiceJpaEntity> findByExternalId(String externalId);

    List<InvoiceJpaEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
