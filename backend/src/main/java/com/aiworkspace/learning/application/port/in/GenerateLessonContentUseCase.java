package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.command.GenerateLessonContentCommand;
import com.aiworkspace.learning.application.dto.CourseDto;

public interface GenerateLessonContentUseCase {
    CourseDto generateContent(GenerateLessonContentCommand command);
}
