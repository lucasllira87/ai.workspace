package com.aiworkspace.audit.application.port.out;

import com.aiworkspace.audit.domain.model.DocumentStats;

import java.util.UUID;

public interface DocumentStatsPort {
    DocumentStats getStatsForUser(UUID userId);
}
