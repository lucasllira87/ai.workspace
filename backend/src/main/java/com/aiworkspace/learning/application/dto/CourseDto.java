package com.aiworkspace.learning.application.dto;

import com.aiworkspace.learning.domain.model.Course;
import com.aiworkspace.learning.domain.model.CourseLevel;
import com.aiworkspace.learning.domain.model.CourseStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CourseDto(UUID id, UUID ownerId, String title, String description,
                         CourseLevel level, String category, CourseStatus status,
                         List<LessonDto> lessons, Instant createdAt, Instant updatedAt) {
    public static CourseDto from(Course course) {
        return new CourseDto(
                course.getId(), course.getOwnerId(), course.getTitle(),
                course.getDescription(), course.getLevel(), course.getCategory(),
                course.getStatus(),
                course.getLessons().stream().map(LessonDto::from).toList(),
                course.getCreatedAt(), course.getUpdatedAt());
    }
}
