package com.aiworkspace.billing.application.port.in;

import com.aiworkspace.billing.application.dto.PlanDto;

import java.util.List;

public interface ListPlansUseCase {
    List<PlanDto> listActive();
}
