package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.command.AskTutorCommand;
import com.aiworkspace.learning.application.dto.TutorAnswerDto;

public interface AskTutorUseCase {
    TutorAnswerDto ask(AskTutorCommand command);
}
