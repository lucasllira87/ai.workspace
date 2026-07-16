package com.aiworkspace.aicore.application.port.out.provider;

import com.aiworkspace.aicore.domain.model.ProviderId;

/**
 * Port for content moderation. No MVP implementation — prepared for v1.1.
 */
public interface ModerationProvider {

    ProviderId getProviderId();

    ModerationResult moderate(String text);

    record ModerationResult(boolean flagged, String category, double score) {}
}
