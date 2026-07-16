package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.command.GenerateQuizCommand;
import com.aiworkspace.learning.application.dto.CourseDto;

public interface GenerateQuizUseCase {
    CourseDto generateQuiz(GenerateQuizCommand command);
}
