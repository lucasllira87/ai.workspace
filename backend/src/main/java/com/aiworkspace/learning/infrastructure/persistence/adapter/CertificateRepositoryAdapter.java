package com.aiworkspace.learning.infrastructure.persistence.adapter;

import com.aiworkspace.learning.application.port.out.CertificateRepository;
import com.aiworkspace.learning.domain.model.Certificate;
import com.aiworkspace.learning.infrastructure.persistence.entity.CertificateJpaEntity;
import com.aiworkspace.learning.infrastructure.persistence.repository.CertificateJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CertificateRepositoryAdapter implements CertificateRepository {

    private final CertificateJpaRepository jpaRepository;

    public CertificateRepositoryAdapter(CertificateJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Certificate save(Certificate certificate) {
        jpaRepository.save(toEntity(certificate));
        return certificate;
    }

    @Override
    public Optional<Certificate> findByEnrollmentId(UUID enrollmentId) {
        return jpaRepository.findByEnrollmentId(enrollmentId).map(this::toDomain);
    }

    private CertificateJpaEntity toEntity(Certificate certificate) {
        CertificateJpaEntity entity = new CertificateJpaEntity();
        entity.setId(certificate.getId());
        entity.setEnrollmentId(certificate.getEnrollmentId());
        entity.setUserId(certificate.getUserId());
        entity.setCourseId(certificate.getCourseId());
        entity.setCertificateCode(certificate.getCertificateCode());
        entity.setIssuedAt(certificate.getIssuedAt());
        return entity;
    }

    private Certificate toDomain(CertificateJpaEntity entity) {
        return Certificate.reconstitute(
                entity.getId(),
                entity.getEnrollmentId(),
                entity.getUserId(),
                entity.getCourseId(),
                entity.getCertificateCode(),
                entity.getIssuedAt()
        );
    }
}
