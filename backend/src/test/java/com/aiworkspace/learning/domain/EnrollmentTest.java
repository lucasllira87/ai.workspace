package com.aiworkspace.learning.domain;

import com.aiworkspace.learning.domain.event.CourseCompletedEvent;
import com.aiworkspace.learning.domain.event.EnrollmentCreatedEvent;
import com.aiworkspace.learning.domain.event.LessonCompletedEvent;
import com.aiworkspace.learning.domain.exception.LessonNotFoundException;
import com.aiworkspace.learning.domain.exception.LessonNotStartedException;
import com.aiworkspace.learning.domain.model.Enrollment;
import com.aiworkspace.learning.domain.model.EnrollmentStatus;
import com.aiworkspace.shared.domain.DomainEvent;
import com.aiworkspace.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnrollmentTest {

    private static final UUID ENROLLMENT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID COURSE_ID = UUID.randomUUID();
    private static final UUID LESSON_1 = UUID.randomUUID();
    private static final UUID LESSON_2 = UUID.randomUUID();

    @Test
    void enroll_createsActiveEnrollmentWithEvent() {
        Enrollment enrollment = Enrollment.enroll(ENROLLMENT_ID, USER_ID, COURSE_ID,
                List.of(LESSON_1, LESSON_2));

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(enrollment.getUserId()).isEqualTo(USER_ID);
        assertThat(enrollment.getCourseId()).isEqualTo(COURSE_ID);
        assertThat(enrollment.getLessonProgressMap()).hasSize(2);

        List<DomainEvent> events = enrollment.getDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(EnrollmentCreatedEvent.class);
    }

    @Test
    void completeLesson_afterStart_emitsLessonCompletedEvent() {
        Enrollment enrollment = Enrollment.enroll(ENROLLMENT_ID, USER_ID, COURSE_ID, List.of(LESSON_1, LESSON_2));
        enrollment.clearDomainEvents();

        enrollment.startLesson(LESSON_1);
        enrollment.completeLesson(LESSON_1);

        List<DomainEvent> events = enrollment.getDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(LessonCompletedEvent.class);
        LessonCompletedEvent event = (LessonCompletedEvent) events.get(0);
        assertThat(event.lessonId()).isEqualTo(LESSON_1);
    }

    @Test
    void completeLesson_withoutStart_throwsLessonNotStartedException() {
        Enrollment enrollment = Enrollment.enroll(ENROLLMENT_ID, USER_ID, COURSE_ID, List.of(LESSON_1));

        assertThatThrownBy(() -> enrollment.completeLesson(LESSON_1))
                .isInstanceOf(LessonNotStartedException.class);
    }

    @Test
    void completeLesson_unknownLesson_throwsLessonNotFoundException() {
        Enrollment enrollment = Enrollment.enroll(ENROLLMENT_ID, USER_ID, COURSE_ID, List.of(LESSON_1));

        assertThatThrownBy(() -> enrollment.completeLesson(UUID.randomUUID()))
                .isInstanceOf(LessonNotFoundException.class);
    }

    @Test
    void completingAllLessons_transitionsEnrollmentToCompleted() {
        Enrollment enrollment = Enrollment.enroll(ENROLLMENT_ID, USER_ID, COURSE_ID,
                List.of(LESSON_1, LESSON_2));
        enrollment.clearDomainEvents();

        enrollment.startLesson(LESSON_1);
        enrollment.completeLesson(LESSON_1);
        enrollment.startLesson(LESSON_2);
        enrollment.completeLesson(LESSON_2);

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(enrollment.getCompletedAt()).isNotNull();

        boolean hasCourseCompleted = enrollment.getDomainEvents().stream()
                .anyMatch(e -> e instanceof CourseCompletedEvent);
        assertThat(hasCourseCompleted).isTrue();
    }

    @Test
    void completingOnlyOneLessonDoesNotCompleteEnrollment() {
        Enrollment enrollment = Enrollment.enroll(ENROLLMENT_ID, USER_ID, COURSE_ID,
                List.of(LESSON_1, LESSON_2));

        enrollment.startLesson(LESSON_1);
        enrollment.completeLesson(LESSON_1);

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
    }

    @Test
    void drop_changesStatusToDropped() {
        Enrollment enrollment = Enrollment.enroll(ENROLLMENT_ID, USER_ID, COURSE_ID, List.of(LESSON_1));

        enrollment.drop();

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.DROPPED);
    }

    @Test
    void startLesson_onDroppedEnrollment_throwsDomainException() {
        Enrollment enrollment = Enrollment.enroll(ENROLLMENT_ID, USER_ID, COURSE_ID, List.of(LESSON_1));
        enrollment.drop();

        assertThatThrownBy(() -> enrollment.startLesson(LESSON_1))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void enroll_withEmptyLessonList_cannotComplete() {
        Enrollment enrollment = Enrollment.enroll(ENROLLMENT_ID, USER_ID, COURSE_ID, List.of());

        // Enrollment with no lessons cannot auto-complete
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
    }
}
