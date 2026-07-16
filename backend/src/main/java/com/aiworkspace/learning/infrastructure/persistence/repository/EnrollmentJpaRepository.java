package com.aiworkspace.learning.infrastructure.persistence.repository;

import com.aiworkspace.learning.infrastructure.persistence.entity.EnrollmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentJpaRepository extends JpaRepository<EnrollmentJpaEntity, UUID> {

    Optional<EnrollmentJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    List<EnrollmentJpaEntity> findAllByUserIdOrderByEnrolledAtDesc(UUID userId);

    @Query("SELECT COUNT(e) > 0 FROM EnrollmentJpaEntity e WHERE e.courseId = :courseId AND e.userId = :userId AND e.status = 'ACTIVE'")
    boolean existsActiveByCourseIdAndUserId(@Param("courseId") UUID courseId, @Param("userId") UUID userId);

    // Dashboard stats queries
    long countByUserId(UUID userId);

    long countByUserIdAndStatus(UUID userId, String status);
}
