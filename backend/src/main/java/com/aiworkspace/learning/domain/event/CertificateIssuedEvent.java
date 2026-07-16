package com.aiworkspace.learning.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CertificateIssuedEvent(UUID certificateId, UUID enrollmentId,
                                      UUID userId, UUID courseId,
                                      String certificateCode, Instant issuedAt) {}
