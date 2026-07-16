package com.aiworkspace.learning.presentation.response;

import com.aiworkspace.learning.application.dto.CertificateDto;

import java.time.Instant;
import java.util.UUID;

public record CertificateResponse(
        UUID id,
        UUID enrollmentId,
        UUID userId,
        UUID courseId,
        String certificateCode,
        Instant issuedAt
) {
    public static CertificateResponse from(CertificateDto dto) {
        return new CertificateResponse(dto.id(), dto.enrollmentId(), dto.userId(),
                dto.courseId(), dto.certificateCode(), dto.issuedAt());
    }
}
