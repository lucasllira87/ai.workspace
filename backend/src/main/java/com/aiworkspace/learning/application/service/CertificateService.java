package com.aiworkspace.learning.application.service;

import com.aiworkspace.learning.application.dto.CertificateDto;
import com.aiworkspace.learning.application.port.in.GetCertificateUseCase;
import com.aiworkspace.learning.application.port.out.CertificateRepository;
import com.aiworkspace.learning.application.port.out.EnrollmentRepository;
import com.aiworkspace.learning.domain.event.CertificateIssuedEvent;
import com.aiworkspace.learning.domain.event.CourseCompletedEvent;
import com.aiworkspace.learning.domain.exception.CertificateNotFoundException;
import com.aiworkspace.learning.domain.exception.EnrollmentNotFoundException;
import com.aiworkspace.learning.domain.model.Certificate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Service
public class CertificateService implements GetCertificateUseCase {

    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CertificateService(CertificateRepository certificateRepository,
                               EnrollmentRepository enrollmentRepository,
                               ApplicationEventPublisher eventPublisher) {
        this.certificateRepository = certificateRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void onCourseCompleted(CourseCompletedEvent event) {
        Certificate certificate = Certificate.issue(
                UUID.randomUUID(), event.enrollmentId(), event.userId(), event.courseId());
        Certificate saved = certificateRepository.save(certificate);
        eventPublisher.publishEvent(new CertificateIssuedEvent(
                saved.getId(), saved.getEnrollmentId(), saved.getUserId(),
                saved.getCourseId(), saved.getCertificateCode(), saved.getIssuedAt()));
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateDto getByEnrollment(UUID enrollmentId, UUID userId) {
        enrollmentRepository.findByIdAndUserId(enrollmentId, userId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        return certificateRepository.findByEnrollmentId(enrollmentId)
                .map(CertificateDto::from)
                .orElseThrow(() -> new CertificateNotFoundException(enrollmentId));
    }
}
