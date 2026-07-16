package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.command.UpdateLessonCommand;
import com.aiworkspace.learning.application.dto.CourseDto;

public interface UpdateLessonUseCase {
    CourseDto updateLesson(UpdateLessonCommand command);
}
