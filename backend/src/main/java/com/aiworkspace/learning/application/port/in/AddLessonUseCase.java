package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.command.AddLessonCommand;
import com.aiworkspace.learning.application.dto.CourseDto;

public interface AddLessonUseCase {
    CourseDto addLesson(AddLessonCommand command);
}
