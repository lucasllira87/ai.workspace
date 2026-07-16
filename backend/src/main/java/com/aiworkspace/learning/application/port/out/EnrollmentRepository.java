package com.aiworkspace.learning.application.port.out;

import com.aiworkspace.learning.domain.model.Enrollment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository {
    Enrollment save(Enrollment enrollment);
    Optional<Enrollment> findByIdAndUserId(UUID enrollmentId, UUID userId);
    List<Enrollment> findAllByUserId(UUID userId);
    boolean existsActiveByCourseIdAndUserId(UUID courseId, UUID userId);
}
