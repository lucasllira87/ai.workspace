package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.dto.EnrollmentDto;

import java.util.List;
import java.util.UUID;

public interface ListEnrollmentsUseCase {
    List<EnrollmentDto> listByUser(UUID userId);
}
