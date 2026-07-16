package com.aiworkspace.learning.application.service;

import com.aiworkspace.learning.application.command.AskTutorCommand;
import com.aiworkspace.learning.application.dto.TutorAnswerDto;
import com.aiworkspace.learning.application.port.in.AskTutorUseCase;
import com.aiworkspace.learning.application.port.out.AITutorPort;
import com.aiworkspace.learning.application.port.out.CourseRepository;
import com.aiworkspace.learning.application.port.out.EnrollmentRepository;
import com.aiworkspace.learning.domain.exception.CourseNotFoundException;
import com.aiworkspace.learning.domain.exception.EnrollmentNotFoundException;
import com.aiworkspace.learning.domain.model.Course;
import com.aiworkspace.learning.domain.model.Enrollment;
import com.aiworkspace.learning.domain.model.EnrollmentStatus;
import com.aiworkspace.learning.domain.model.Lesson;
import com.aiworkspace.shared.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TutorService implements AskTutorUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final AITutorPort aiTutorPort;

    public TutorService(EnrollmentRepository enrollmentRepository,
                         CourseRepository courseRepository,
                         AITutorPort aiTutorPort) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.aiTutorPort = aiTutorPort;
    }

    @Override
    @Transactional(readOnly = true)
    public TutorAnswerDto ask(AskTutorCommand command) {
        Enrollment enrollment = enrollmentRepository.findByIdAndUserId(
                command.enrollmentId(), command.userId())
                .orElseThrow(() -> new EnrollmentNotFoundException(command.enrollmentId()));

        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE
                && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            throw new DomainException("Cannot use tutor for a dropped enrollment");
        }

        Course course = courseRepository.findById(enrollment.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException(enrollment.getCourseId()));

        Lesson lesson = course.getLessonById(command.lessonId());

        if (lesson.getContent() == null || lesson.getContent().isBlank()) {
            throw new DomainException("This lesson has no text content for the AI tutor");
        }

        String answer = aiTutorPort.answer(
                command.question(), lesson.getContent(), lesson.getTitle());

        return new TutorAnswerDto(command.lessonId(), command.question(), answer);
    }
}
