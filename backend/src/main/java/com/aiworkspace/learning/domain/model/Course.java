package com.aiworkspace.learning.domain.model;

import com.aiworkspace.learning.domain.event.CourseCreatedEvent;
import com.aiworkspace.learning.domain.event.CoursePublishedEvent;
import com.aiworkspace.learning.domain.exception.LessonNotFoundException;
import com.aiworkspace.shared.domain.AggregateRoot;
import com.aiworkspace.shared.exception.DomainException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Course extends AggregateRoot {

    private final UUID id;
    private final UUID ownerId;
    private String title;
    private String description;
    private CourseLevel level;
    private String category;
    private CourseStatus status;
    private final List<Lesson> lessons;
    private final Instant createdAt;
    private Instant updatedAt;

    private Course(UUID id, UUID ownerId, String title, String description,
                   CourseLevel level, String category, CourseStatus status,
                   List<Lesson> lessons, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.level = level;
        this.category = category;
        this.status = status;
        this.lessons = new ArrayList<>(lessons);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Course create(UUID id, UUID ownerId, String title, String description,
                                 CourseLevel level, String category) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(category, "category");

        Instant now = Instant.now();
        Course course = new Course(id, ownerId, title, description, level, category,
                CourseStatus.DRAFT, List.of(), now, now);
        course.registerEvent(new CourseCreatedEvent(id, ownerId, title, now));
        return course;
    }

    public static Course reconstitute(UUID id, UUID ownerId, String title, String description,
                                       CourseLevel level, String category, CourseStatus status,
                                       List<Lesson> lessons, Instant createdAt, Instant updatedAt) {
        return new Course(id, ownerId, title, description, level, category,
                status, lessons, createdAt, updatedAt);
    }

    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
        updatedAt = Instant.now();
    }

    public void updateLesson(UUID lessonId, String title, String content,
                              String videoUrl, int estimatedMinutes) {
        findLesson(lessonId).update(title, content, videoUrl, estimatedMinutes);
        updatedAt = Instant.now();
    }

    public void updateLessonContent(UUID lessonId, String content) {
        findLesson(lessonId).updateContent(content);
        updatedAt = Instant.now();
    }

    public void setLessonQuizQuestions(UUID lessonId, List<QuizQuestion> questions) {
        findLesson(lessonId).setQuizQuestions(questions);
        updatedAt = Instant.now();
    }

    public void removeLesson(UUID lessonId) {
        if (status == CourseStatus.PUBLISHED && lessons.size() == 1) {
            throw new DomainException("Cannot remove the last lesson from a published course");
        }
        boolean removed = lessons.removeIf(l -> l.getId().equals(lessonId));
        if (!removed) {
            throw new LessonNotFoundException(lessonId);
        }
        reorderLessons();
        updatedAt = Instant.now();
    }

    public void publish() {
        if (lessons.isEmpty()) {
            throw new DomainException("Cannot publish a course with no lessons");
        }
        if (status == CourseStatus.ARCHIVED) {
            throw new DomainException("Cannot publish an archived course");
        }
        this.status = CourseStatus.PUBLISHED;
        this.updatedAt = Instant.now();
        registerEvent(new CoursePublishedEvent(id, ownerId, title, lessons.size(), updatedAt));
    }

    public void archive() {
        if (status == CourseStatus.DRAFT) {
            throw new DomainException("Cannot archive a draft course — publish it first");
        }
        this.status = CourseStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    public Lesson getLessonById(UUID lessonId) {
        return findLesson(lessonId);
    }

    public List<UUID> getLessonIds() {
        return lessons.stream().map(Lesson::getId).toList();
    }

    private Lesson findLesson(UUID lessonId) {
        return lessons.stream()
                .filter(l -> l.getId().equals(lessonId))
                .findFirst()
                .orElseThrow(() -> new LessonNotFoundException(lessonId));
    }

    private void reorderLessons() {
        for (int i = 0; i < lessons.size(); i++) {
            lessons.get(i).setLessonOrder(i + 1);
        }
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public CourseLevel getLevel() { return level; }
    public String getCategory() { return category; }
    public CourseStatus getStatus() { return status; }
    public List<Lesson> getLessons() { return Collections.unmodifiableList(lessons); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
