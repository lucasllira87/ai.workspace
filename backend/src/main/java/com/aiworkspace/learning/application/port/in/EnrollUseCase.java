package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.command.EnrollCommand;
import com.aiworkspace.learning.application.dto.EnrollmentDto;

public interface EnrollUseCase {
    EnrollmentDto enroll(EnrollCommand command);
}
