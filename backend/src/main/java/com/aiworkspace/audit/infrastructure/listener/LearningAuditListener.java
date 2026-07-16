package com.aiworkspace.audit.infrastructure.listener;

import com.aiworkspace.audit.application.command.RecordAuditEventCommand;
import com.aiworkspace.audit.application.port.in.RecordAuditEventUseCase;
import com.aiworkspace.audit.domain.model.AuditEventType;
import com.aiworkspace.learning.domain.event.CertificateIssuedEvent;
import com.aiworkspace.learning.domain.event.CourseCompletedEvent;
import com.aiworkspace.learning.domain.event.CourseCreatedEvent;
import com.aiworkspace.learning.domain.event.CoursePublishedEvent;
import com.aiworkspace.learning.domain.event.EnrollmentCreatedEvent;
import com.aiworkspace.learning.domain.event.LessonCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
public class LearningAuditListener {

    private static final Logger log = LoggerFactory.getLogger(LearningAuditListener.class);

    private final RecordAuditEventUseCase recordAuditEvent;

    public LearningAuditListener(RecordAuditEventUseCase recordAuditEvent) {
        this.recordAuditEvent = recordAuditEvent;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCourseCreated(CourseCreatedEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.ownerId(), AuditEventType.COURSE_CREATED, "learning",
                "Created course: " + event.title(),
                Map.of("courseId", event.courseId().toString())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCoursePublished(CoursePublishedEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.ownerId(), AuditEventType.COURSE_PUBLISHED, "learning",
                "Published course: " + event.title(),
                Map.of("courseId", event.courseId().toString())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEnrollmentCreated(EnrollmentCreatedEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.userId(), AuditEventType.ENROLLED, "learning",
                "Enrolled in course",
                Map.of("enrollmentId", event.enrollmentId().toString(),
                       "courseId", event.courseId().toString())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLessonCompleted(LessonCompletedEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.userId(), AuditEventType.LESSON_COMPLETED, "learning",
                "Completed a lesson",
                Map.of("enrollmentId", event.enrollmentId().toString(),
                       "lessonId", event.lessonId().toString())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCourseCompleted(CourseCompletedEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.userId(), AuditEventType.COURSE_COMPLETED, "learning",
                "Completed course",
                Map.of("enrollmentId", event.enrollmentId().toString(),
                       "courseId", event.courseId().toString())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCertificateIssued(CertificateIssuedEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.userId(), AuditEventType.CERTIFICATE_ISSUED, "learning",
                "Certificate issued",
                Map.of("certificateId", event.certificateId().toString(),
                       "courseId", event.courseId().toString(),
                       "certificateCode", event.certificateCode())
        ));
    }

    private void recordSafely(RecordAuditEventCommand command) {
        try {
            recordAuditEvent.record(command);
        } catch (Exception e) {
            log.error("Failed to record audit event: type={} reason={}",
                    command.eventType(), e.getMessage());
        }
    }
}
