package com.aiworkspace.learning.infrastructure.persistence.repository;

import com.aiworkspace.learning.infrastructure.persistence.entity.CertificateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CertificateJpaRepository extends JpaRepository<CertificateJpaEntity, UUID> {

    Optional<CertificateJpaEntity> findByEnrollmentId(UUID enrollmentId);

    // Dashboard stats query
    long countByUserId(UUID userId);
}
