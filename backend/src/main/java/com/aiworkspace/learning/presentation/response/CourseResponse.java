package com.aiworkspace.learning.presentation.response;

import com.aiworkspace.learning.application.dto.CourseDto;
import com.aiworkspace.learning.application.dto.LessonDto;
import com.aiworkspace.learning.domain.model.CourseLevel;
import com.aiworkspace.learning.domain.model.CourseStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        UUID ownerId,
        String title,
        String description,
        CourseLevel level,
        String category,
        CourseStatus status,
        List<LessonDto> lessons,
        Instant createdAt,
        Instant updatedAt
) {
    public static CourseResponse from(CourseDto dto) {
        return new CourseResponse(dto.id(), dto.ownerId(), dto.title(), dto.description(),
                dto.level(), dto.category(), dto.status(), dto.lessons(),
                dto.createdAt(), dto.updatedAt());
    }
}
