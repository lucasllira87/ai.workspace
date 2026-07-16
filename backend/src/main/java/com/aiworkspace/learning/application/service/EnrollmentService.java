package com.aiworkspace.learning.application.service;

import com.aiworkspace.learning.application.command.EnrollCommand;
import com.aiworkspace.learning.application.dto.EnrollmentDto;
import com.aiworkspace.learning.application.port.in.CompleteLessonUseCase;
import com.aiworkspace.learning.application.port.in.DropEnrollmentUseCase;
import com.aiworkspace.learning.application.port.in.EnrollUseCase;
import com.aiworkspace.learning.application.port.in.GetEnrollmentUseCase;
import com.aiworkspace.learning.application.port.in.ListEnrollmentsUseCase;
import com.aiworkspace.learning.application.port.in.StartLessonUseCase;
import com.aiworkspace.learning.application.port.out.CourseRepository;
import com.aiworkspace.learning.application.port.out.EnrollmentRepository;
import com.aiworkspace.learning.domain.exception.AlreadyEnrolledException;
import com.aiworkspace.learning.domain.exception.CourseNotFoundException;
import com.aiworkspace.learning.domain.exception.CourseNotPublishedException;
import com.aiworkspace.learning.domain.exception.EnrollmentNotFoundException;
import com.aiworkspace.learning.domain.model.Course;
import com.aiworkspace.learning.domain.model.CourseStatus;
import com.aiworkspace.learning.domain.model.Enrollment;
import com.aiworkspace.learning.domain.model.EnrollmentStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EnrollmentService implements EnrollUseCase, StartLessonUseCase,
        CompleteLessonUseCase, DropEnrollmentUseCase, GetEnrollmentUseCase, ListEnrollmentsUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Counter enrollmentCreatedCounter;
    private final Counter lessonCompletedCounter;
    private final Counter courseCompletedCounter;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                              CourseRepository courseRepository,
                              ApplicationEventPublisher eventPublisher,
                              MeterRegistry meterRegistry) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.eventPublisher = eventPublisher;
        this.enrollmentCreatedCounter = Counter.builder("learning.enrollments.created")
                .description("Total enrollments created").register(meterRegistry);
        this.lessonCompletedCounter = Counter.builder("learning.lessons.completed")
                .description("Total lessons completed").register(meterRegistry);
        this.courseCompletedCounter = Counter.builder("learning.courses.completed")
                .description("Total courses completed by students").register(meterRegistry);
    }

    @Override
    @Transactional
    public EnrollmentDto enroll(EnrollCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new CourseNotFoundException(command.courseId()));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new CourseNotPublishedException(command.courseId());
        }

        if (enrollmentRepository.existsActiveByCourseIdAndUserId(command.courseId(), command.userId())) {
            throw new AlreadyEnrolledException(command.courseId());
        }

        Enrollment enrollment = Enrollment.enroll(
                UUID.randomUUID(), command.userId(), command.courseId(), course.getLessonIds());
        Enrollment saved = enrollmentRepository.save(enrollment);
        enrollment.getDomainEvents().forEach(eventPublisher::publishEvent);
        enrollmentCreatedCounter.increment();
        return EnrollmentDto.from(saved);
    }

    @Override
    @Transactional
    public EnrollmentDto startLesson(UUID enrollmentId, UUID lessonId, UUID userId) {
        Enrollment enrollment = loadEnrollmentForUser(enrollmentId, userId);
        enrollment.startLesson(lessonId);
        Enrollment saved = enrollmentRepository.save(enrollment);
        return EnrollmentDto.from(saved);
    }

    @Override
    @Transactional
    public EnrollmentDto completeLesson(UUID enrollmentId, UUID lessonId, UUID userId) {
        Enrollment enrollment = loadEnrollmentForUser(enrollmentId, userId);
        enrollment.completeLesson(lessonId);
        Enrollment saved = enrollmentRepository.save(enrollment);
        enrollment.getDomainEvents().forEach(eventPublisher::publishEvent);
        lessonCompletedCounter.increment();
        if (saved.getStatus() == EnrollmentStatus.COMPLETED) {
            courseCompletedCounter.increment();
        }
        return EnrollmentDto.from(saved);
    }

    @Override
    @Transactional
    public void drop(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = loadEnrollmentForUser(enrollmentId, userId);
        enrollment.drop();
        enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentDto getById(UUID enrollmentId, UUID userId) {
        return enrollmentRepository.findByIdAndUserId(enrollmentId, userId)
                .map(EnrollmentDto::from)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDto> listByUser(UUID userId) {
        return enrollmentRepository.findAllByUserId(userId)
                .stream().map(EnrollmentDto::from).toList();
    }

    private Enrollment loadEnrollmentForUser(UUID enrollmentId, UUID userId) {
        return enrollmentRepository.findByIdAndUserId(enrollmentId, userId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));
    }
}
