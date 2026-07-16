package com.aiworkspace.audit.application.port.out;

import com.aiworkspace.audit.domain.model.LearningStats;

import java.util.UUID;

public interface LearningStatsPort {
    LearningStats getStatsForUser(UUID userId);
}
