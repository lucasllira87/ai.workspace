package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.dto.CertificateDto;

import java.util.UUID;

public interface GetCertificateUseCase {
    CertificateDto getByEnrollment(UUID enrollmentId, UUID userId);
}
