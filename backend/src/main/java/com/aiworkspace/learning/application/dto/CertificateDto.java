package com.aiworkspace.learning.application.dto;

import com.aiworkspace.learning.domain.model.Certificate;

import java.time.Instant;
import java.util.UUID;

public record CertificateDto(UUID id, UUID enrollmentId, UUID userId, UUID courseId,
                              String certificateCode, Instant issuedAt) {
    public static CertificateDto from(Certificate cert) {
        return new CertificateDto(cert.getId(), cert.getEnrollmentId(), cert.getUserId(),
                cert.getCourseId(), cert.getCertificateCode(), cert.getIssuedAt());
    }
}
