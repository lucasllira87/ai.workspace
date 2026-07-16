package com.aiworkspace.learning.application.port.out;

import com.aiworkspace.learning.domain.model.Certificate;

import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository {
    Certificate save(Certificate certificate);
    Optional<Certificate> findByEnrollmentId(UUID enrollmentId);
}
