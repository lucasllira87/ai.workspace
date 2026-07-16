package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.dto.CourseDto;

import java.util.List;
import java.util.UUID;

public interface ListCoursesUseCase {
    List<CourseDto> listPublished();
    List<CourseDto> listOwned(UUID ownerId);
}
