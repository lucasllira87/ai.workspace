package com.aiworkspace.aicore.application.port.out.provider;

import com.aiworkspace.aicore.domain.model.ModelId;
import com.aiworkspace.aicore.domain.model.ProviderId;

import java.util.Set;

/**
 * Port for image generation. No MVP implementation — prepared for v1.1.
 */
public interface ImageGenerationProvider {

    ProviderId getProviderId();

    Set<ModelId> getSupportedModels();

    ImageResponse generate(ImageRequest request);

    record ImageRequest(String prompt, int width, int height, String style) {}

    record ImageResponse(byte[] imageData, String mimeType, ModelId model, ProviderId provider) {}
}
