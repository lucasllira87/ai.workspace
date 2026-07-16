package com.aiworkspace.learning.domain.model;

import com.aiworkspace.learning.domain.event.CourseCompletedEvent;
import com.aiworkspace.learning.domain.event.EnrollmentCreatedEvent;
import com.aiworkspace.learning.domain.event.LessonCompletedEvent;
import com.aiworkspace.learning.domain.exception.LessonNotFoundException;
import com.aiworkspace.learning.domain.exception.LessonNotStartedException;
import com.aiworkspace.shared.domain.AggregateRoot;
import com.aiworkspace.shared.exception.DomainException;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Enrollment extends AggregateRoot {

    private final UUID id;
    private final UUID userId;
    private final UUID courseId;
    private EnrollmentStatus status;
    private final Map<UUID, LessonProgress> lessonProgressMap;
    private final Instant enrolledAt;
    private Instant completedAt;

    private Enrollment(UUID id, UUID userId, UUID courseId, EnrollmentStatus status,
                        Map<UUID, LessonProgress> lessonProgressMap,
                        Instant enrolledAt, Instant completedAt) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.status = status;
        this.lessonProgressMap = new HashMap<>(lessonProgressMap);
        this.enrolledAt = enrolledAt;
        this.completedAt = completedAt;
    }

    public static Enrollment enroll(UUID id, UUID userId, UUID courseId, List<UUID> lessonIds) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(courseId, "courseId");

        Instant now = Instant.now();
        Map<UUID, LessonProgress> progress = new HashMap<>();
        for (UUID lessonId : lessonIds) {
            progress.put(lessonId, LessonProgress.notStarted(lessonId));
        }

        Enrollment enrollment = new Enrollment(id, userId, courseId,
                EnrollmentStatus.ACTIVE, progress, now, null);
        enrollment.registerEvent(new EnrollmentCreatedEvent(id, userId, courseId, now));
        return enrollment;
    }

    public static Enrollment reconstitute(UUID id, UUID userId, UUID courseId,
                                           EnrollmentStatus status,
                                           Map<UUID, LessonProgress> lessonProgressMap,
                                           Instant enrolledAt, Instant completedAt) {
        return new Enrollment(id, userId, courseId, status, lessonProgressMap, enrolledAt, completedAt);
    }

    public void startLesson(UUID lessonId) {
        ensureActive();
        LessonProgress progress = getProgressOrThrow(lessonId);
        if (progress.status() == LessonProgressStatus.COMPLETED) return;
        lessonProgressMap.put(lessonId, progress.start());
    }

    public void completeLesson(UUID lessonId) {
        ensureActive();
        LessonProgress progress = getProgressOrThrow(lessonId);

        if (progress.status() == LessonProgressStatus.NOT_STARTED) {
            throw new LessonNotStartedException(lessonId);
        }
        if (progress.status() == LessonProgressStatus.COMPLETED) return;

        lessonProgressMap.put(lessonId, progress.complete());
        registerEvent(new LessonCompletedEvent(id, userId, courseId, lessonId, Instant.now()));

        if (allLessonsCompleted()) {
            complete();
        }
    }

    public void drop() {
        ensureActive();
        this.status = EnrollmentStatus.DROPPED;
    }

    private void complete() {
        this.status = EnrollmentStatus.COMPLETED;
        this.completedAt = Instant.now();
        registerEvent(new CourseCompletedEvent(id, userId, courseId, completedAt));
    }

    private void ensureActive() {
        if (status != EnrollmentStatus.ACTIVE) {
            throw new DomainException("Enrollment is not active — current status: " + status);
        }
    }

    private boolean allLessonsCompleted() {
        if (lessonProgressMap.isEmpty()) return false;
        return lessonProgressMap.values().stream()
                .allMatch(p -> p.status() == LessonProgressStatus.COMPLETED);
    }

    private LessonProgress getProgressOrThrow(UUID lessonId) {
        LessonProgress progress = lessonProgressMap.get(lessonId);
        if (progress == null) throw new LessonNotFoundException(lessonId);
        return progress;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getCourseId() { return courseId; }
    public EnrollmentStatus getStatus() { return status; }
    public Map<UUID, LessonProgress> getLessonProgressMap() {
        return Collections.unmodifiableMap(lessonProgressMap);
    }
    public Instant getEnrolledAt() { return enrolledAt; }
    public Instant getCompletedAt() { return completedAt; }
}
