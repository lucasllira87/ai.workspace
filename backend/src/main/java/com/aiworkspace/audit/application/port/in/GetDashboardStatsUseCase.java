package com.aiworkspace.audit.application.port.in;

import com.aiworkspace.audit.domain.model.DashboardStats;

import java.util.UUID;

public interface GetDashboardStatsUseCase {
    DashboardStats getFor(UUID userId);
}
