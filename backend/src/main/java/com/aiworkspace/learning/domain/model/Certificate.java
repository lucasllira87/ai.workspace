package com.aiworkspace.learning.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Certificate {

    private final UUID id;
    private final UUID enrollmentId;
    private final UUID userId;
    private final UUID courseId;
    private final String certificateCode;
    private final Instant issuedAt;

    private Certificate(UUID id, UUID enrollmentId, UUID userId, UUID courseId,
                         String certificateCode, Instant issuedAt) {
        this.id = id;
        this.enrollmentId = enrollmentId;
        this.userId = userId;
        this.courseId = courseId;
        this.certificateCode = certificateCode;
        this.issuedAt = issuedAt;
    }

    public static Certificate issue(UUID id, UUID enrollmentId, UUID userId, UUID courseId) {
        String code = UUID.randomUUID().toString().toUpperCase().replace("-", "");
        return new Certificate(id, enrollmentId, userId, courseId, code, Instant.now());
    }

    public static Certificate reconstitute(UUID id, UUID enrollmentId, UUID userId, UUID courseId,
                                            String certificateCode, Instant issuedAt) {
        return new Certificate(id, enrollmentId, userId, courseId, certificateCode, issuedAt);
    }

    public UUID getId() { return id; }
    public UUID getEnrollmentId() { return enrollmentId; }
    public UUID getUserId() { return userId; }
    public UUID getCourseId() { return courseId; }
    public String getCertificateCode() { return certificateCode; }
    public Instant getIssuedAt() { return issuedAt; }
}
