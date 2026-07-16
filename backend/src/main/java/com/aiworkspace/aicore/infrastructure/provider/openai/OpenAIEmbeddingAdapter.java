package com.aiworkspace.aicore.infrastructure.provider.openai;

import com.aiworkspace.aicore.application.dto.EmbeddingRequest;
import com.aiworkspace.aicore.application.dto.EmbeddingResponse;
import com.aiworkspace.aicore.application.port.out.provider.EmbeddingProvider;
import com.aiworkspace.aicore.domain.exception.ProviderUnavailableException;
import com.aiworkspace.aicore.domain.model.EmbeddingVector;
import com.aiworkspace.aicore.domain.model.EstimatedCost;
import com.aiworkspace.aicore.domain.model.ModelId;
import com.aiworkspace.aicore.domain.model.ProviderId;
import com.aiworkspace.aicore.domain.model.TokenUsage;
import com.aiworkspace.aicore.infrastructure.provider.ModelPricingCatalog;
import com.openai.client.OpenAIClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.models.embeddings.EmbeddingModel;

import java.util.List;
import java.util.Set;

public class OpenAIEmbeddingAdapter implements EmbeddingProvider {

    private static final ProviderId PROVIDER_ID = ProviderId.OPENAI;

    private static final Set<ModelId> SUPPORTED_MODELS = Set.of(
            ModelId.TEXT_EMBEDDING_3_SMALL,
            ModelId.TEXT_EMBEDDING_3_LARGE
    );

    private final OpenAIClient client;
    private final ModelPricingCatalog pricingCatalog;

    public OpenAIEmbeddingAdapter(OpenAIClient client, ModelPricingCatalog pricingCatalog) {
        this.client = client;
        this.pricingCatalog = pricingCatalog;
    }

    @Override
    public ProviderId getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public Set<ModelId> getSupportedModels() {
        return SUPPORTED_MODELS;
    }

    @Override
    public boolean supports(ModelId model) {
        return model == null || SUPPORTED_MODELS.contains(model);
    }

    @Override
    public EmbeddingResponse embed(EmbeddingRequest request) {
        long start = System.currentTimeMillis();
        try {
            ModelId model = request.model() != null ? request.model() : ModelId.TEXT_EMBEDDING_3_SMALL;

            EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                    .model(EmbeddingModel.of(model.value()))
                    .inputOfArrayOfStrings(request.texts())
                    .build();

            CreateEmbeddingResponse response = client.embeddings().create(params);

            List<EmbeddingVector> vectors = response.data().stream()
                    .map(e -> {
                        List<Double> doubleValues = e.embedding().asFloatArrayEmbedding();
                        float[] floatValues = new float[doubleValues.size()];
                        for (int i = 0; i < doubleValues.size(); i++) {
                            floatValues[i] = doubleValues.get(i).floatValue();
                        }
                        return EmbeddingVector.of(floatValues, model);
                    })
                    .toList();

            int totalTokens = response.usage() != null
                    ? (int) response.usage().totalTokens() : 0;
            TokenUsage usage = TokenUsage.embeddingUsage(totalTokens);
            EstimatedCost cost = pricingCatalog.estimateCost(model, totalTokens, 0);
            long latency = System.currentTimeMillis() - start;

            return new EmbeddingResponse(vectors, model, PROVIDER_ID, usage, cost, latency);

        } catch (Exception e) {
            throw new ProviderUnavailableException(PROVIDER_ID, e.getMessage(), e);
        }
    }
}
