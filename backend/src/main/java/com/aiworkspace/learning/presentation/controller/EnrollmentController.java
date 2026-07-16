package com.aiworkspace.learning.presentation.controller;

import com.aiworkspace.learning.application.command.EnrollCommand;
import com.aiworkspace.learning.application.port.in.CompleteLessonUseCase;
import com.aiworkspace.learning.application.port.in.DropEnrollmentUseCase;
import com.aiworkspace.learning.application.port.in.EnrollUseCase;
import com.aiworkspace.learning.application.port.in.GetCertificateUseCase;
import com.aiworkspace.learning.application.port.in.GetEnrollmentUseCase;
import com.aiworkspace.learning.application.port.in.ListEnrollmentsUseCase;
import com.aiworkspace.learning.application.port.in.StartLessonUseCase;
import com.aiworkspace.learning.application.port.out.LearningUserContextPort;
import com.aiworkspace.learning.presentation.request.EnrollRequest;
import com.aiworkspace.learning.presentation.response.CertificateResponse;
import com.aiworkspace.learning.presentation.response.EnrollmentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    private final EnrollUseCase enroll;
    private final StartLessonUseCase startLesson;
    private final CompleteLessonUseCase completeLesson;
    private final DropEnrollmentUseCase dropEnrollment;
    private final GetEnrollmentUseCase getEnrollment;
    private final ListEnrollmentsUseCase listEnrollments;
    private final GetCertificateUseCase getCertificate;
    private final LearningUserContextPort userContext;

    public EnrollmentController(EnrollUseCase enroll,
                                 StartLessonUseCase startLesson,
                                 CompleteLessonUseCase completeLesson,
                                 DropEnrollmentUseCase dropEnrollment,
                                 GetEnrollmentUseCase getEnrollment,
                                 ListEnrollmentsUseCase listEnrollments,
                                 GetCertificateUseCase getCertificate,
                                 LearningUserContextPort userContext) {
        this.enroll = enroll;
        this.startLesson = startLesson;
        this.completeLesson = completeLesson;
        this.dropEnrollment = dropEnrollment;
        this.getEnrollment = getEnrollment;
        this.listEnrollments = listEnrollments;
        this.getCertificate = getCertificate;
        this.userContext = userContext;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enroll(@Valid @RequestBody EnrollRequest request) {
        UUID userId = userContext.getCurrentUserId();
        return EnrollmentResponse.from(enroll.enroll(new EnrollCommand(userId, request.courseId())));
    }

    @GetMapping
    public List<EnrollmentResponse> list() {
        UUID userId = userContext.getCurrentUserId();
        return listEnrollments.listByUser(userId).stream().map(EnrollmentResponse::from).toList();
    }

    @GetMapping("/{enrollmentId}")
    public EnrollmentResponse getById(@PathVariable UUID enrollmentId) {
        UUID userId = userContext.getCurrentUserId();
        return EnrollmentResponse.from(getEnrollment.getById(enrollmentId, userId));
    }

    @PostMapping("/{enrollmentId}/lessons/{lessonId}/start")
    public EnrollmentResponse startLesson(@PathVariable UUID enrollmentId,
                                           @PathVariable UUID lessonId) {
        UUID userId = userContext.getCurrentUserId();
        return EnrollmentResponse.from(startLesson.startLesson(enrollmentId, lessonId, userId));
    }

    @PostMapping("/{enrollmentId}/lessons/{lessonId}/complete")
    public EnrollmentResponse completeLesson(@PathVariable UUID enrollmentId,
                                              @PathVariable UUID lessonId) {
        UUID userId = userContext.getCurrentUserId();
        return EnrollmentResponse.from(completeLesson.completeLesson(enrollmentId, lessonId, userId));
    }

    @DeleteMapping("/{enrollmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void drop(@PathVariable UUID enrollmentId) {
        UUID userId = userContext.getCurrentUserId();
        dropEnrollment.drop(enrollmentId, userId);
    }

    @GetMapping("/{enrollmentId}/certificate")
    public CertificateResponse getCertificate(@PathVariable UUID enrollmentId) {
        UUID userId = userContext.getCurrentUserId();
        return CertificateResponse.from(getCertificate.getByEnrollment(enrollmentId, userId));
    }
}
