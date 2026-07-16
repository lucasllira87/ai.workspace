package com.aiworkspace.audit.infrastructure.stats;

import com.aiworkspace.audit.application.port.out.LearningStatsPort;
import com.aiworkspace.audit.domain.model.LearningStats;
import com.aiworkspace.learning.infrastructure.persistence.repository.CertificateJpaRepository;
import com.aiworkspace.learning.infrastructure.persistence.repository.EnrollmentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LearningStatsAdapter implements LearningStatsPort {

    private final EnrollmentJpaRepository enrollmentJpaRepository;
    private final CertificateJpaRepository certificateJpaRepository;

    public LearningStatsAdapter(EnrollmentJpaRepository enrollmentJpaRepository,
                                 CertificateJpaRepository certificateJpaRepository) {
        this.enrollmentJpaRepository = enrollmentJpaRepository;
        this.certificateJpaRepository = certificateJpaRepository;
    }

    @Override
    public LearningStats getStatsForUser(UUID userId) {
        long enrolled = enrollmentJpaRepository.countByUserId(userId);
        long completed = enrollmentJpaRepository.countByUserIdAndStatus(userId, "COMPLETED");
        long certificates = certificateJpaRepository.countByUserId(userId);
        return new LearningStats(enrolled, completed, certificates);
    }
}
