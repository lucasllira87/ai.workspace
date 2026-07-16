package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.dto.CourseDto;

import java.util.UUID;

public interface GetCourseUseCase {
    CourseDto getById(UUID courseId);
}
