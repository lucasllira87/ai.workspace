package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.command.CreateCourseCommand;
import com.aiworkspace.learning.application.dto.CourseDto;

public interface CreateCourseUseCase {
    CourseDto create(CreateCourseCommand command);
}
