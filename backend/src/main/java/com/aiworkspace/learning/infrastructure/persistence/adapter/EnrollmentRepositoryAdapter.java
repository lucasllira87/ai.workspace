package com.aiworkspace.learning.infrastructure.persistence.adapter;

import com.aiworkspace.learning.application.port.out.EnrollmentRepository;
import com.aiworkspace.learning.domain.model.Enrollment;
import com.aiworkspace.learning.domain.model.EnrollmentStatus;
import com.aiworkspace.learning.domain.model.LessonProgress;
import com.aiworkspace.learning.domain.model.LessonProgressStatus;
import com.aiworkspace.learning.infrastructure.persistence.entity.EnrollmentJpaEntity;
import com.aiworkspace.learning.infrastructure.persistence.entity.LessonProgressJpaEntity;
import com.aiworkspace.learning.infrastructure.persistence.repository.EnrollmentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class EnrollmentRepositoryAdapter implements EnrollmentRepository {

    private final EnrollmentJpaRepository jpaRepository;

    public EnrollmentRepositoryAdapter(EnrollmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        EnrollmentJpaEntity entity = toEntity(enrollment);
        jpaRepository.save(entity);
        return enrollment;
    }

    @Override
    public Optional<Enrollment> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    public List<Enrollment> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserIdOrderByEnrolledAtDesc(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByCourseIdAndUserId(UUID courseId, UUID userId) {
        return jpaRepository.existsActiveByCourseIdAndUserId(courseId, userId);
    }

    private EnrollmentJpaEntity toEntity(Enrollment enrollment) {
        EnrollmentJpaEntity entity = new EnrollmentJpaEntity();
        entity.setId(enrollment.getId());
        entity.setUserId(enrollment.getUserId());
        entity.setCourseId(enrollment.getCourseId());
        entity.setStatus(enrollment.getStatus().name());
        entity.setEnrolledAt(enrollment.getEnrolledAt());
        entity.setCompletedAt(enrollment.getCompletedAt());

        List<LessonProgressJpaEntity> progressEntities = new ArrayList<>();
        for (LessonProgress progress : enrollment.getLessonProgressMap().values()) {
            LessonProgressJpaEntity progressEntity = new LessonProgressJpaEntity();
            progressEntity.setId(UUID.randomUUID());
            progressEntity.setEnrollment(entity);
            progressEntity.setLessonId(progress.lessonId());
            progressEntity.setStatus(progress.status().name());
            progressEntity.setStartedAt(progress.startedAt());
            progressEntity.setCompletedAt(progress.completedAt());
            progressEntities.add(progressEntity);
        }
        entity.setLessonProgress(progressEntities);
        return entity;
    }

    private Enrollment toDomain(EnrollmentJpaEntity entity) {
        Map<UUID, LessonProgress> progressMap = new HashMap<>();
        for (LessonProgressJpaEntity pe : entity.getLessonProgress()) {
            LessonProgress progress = new LessonProgress(
                    pe.getLessonId(),
                    LessonProgressStatus.valueOf(pe.getStatus()),
                    pe.getStartedAt(),
                    pe.getCompletedAt()
            );
            progressMap.put(pe.getLessonId(), progress);
        }

        return Enrollment.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getCourseId(),
                EnrollmentStatus.valueOf(entity.getStatus()),
                progressMap,
                entity.getEnrolledAt(),
                entity.getCompletedAt()
        );
    }
}
