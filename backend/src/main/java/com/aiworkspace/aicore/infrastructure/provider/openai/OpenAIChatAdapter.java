package com.aiworkspace.aicore.infrastructure.provider.openai;

import com.aiworkspace.aicore.application.dto.ChatMessage;
import com.aiworkspace.aicore.application.dto.ChatRequest;
import com.aiworkspace.aicore.application.dto.ChatResponse;
import com.aiworkspace.aicore.application.port.out.provider.ChatProvider;
import com.aiworkspace.aicore.domain.exception.ProviderUnavailableException;
import com.aiworkspace.aicore.domain.model.EstimatedCost;
import com.aiworkspace.aicore.domain.model.ModelId;
import com.aiworkspace.aicore.domain.model.ProviderId;
import com.aiworkspace.aicore.domain.model.TokenUsage;
import com.aiworkspace.aicore.infrastructure.provider.ModelPricingCatalog;
import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageParam;

import java.util.List;
import java.util.Set;

public class OpenAIChatAdapter implements ChatProvider {

    private static final ProviderId PROVIDER_ID = ProviderId.OPENAI;

    private static final Set<ModelId> SUPPORTED_MODELS = Set.of(
            ModelId.GPT_4O,
            ModelId.GPT_4O_MINI
    );

    private final OpenAIClient client;
    private final ModelPricingCatalog pricingCatalog;

    public OpenAIChatAdapter(OpenAIClient client, ModelPricingCatalog pricingCatalog) {
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
    public ChatResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();
        try {
            ModelId model = request.model() != null ? request.model() : ModelId.GPT_4O;

            ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder()
                    .model(model.value())
                    .messages(toOpenAIMessages(request.messages()));

            if (request.temperature() != null) {
                builder.temperature(request.temperature());
            }
            if (request.maxTokens() != null) {
                builder.maxCompletionTokens(request.maxTokens());
            }

            ChatCompletion completion = client.chat().completions().create(builder.build());

            String content = completion.choices().get(0).message().content().orElse("");
            int promptTokens = completion.usage()
                    .map(u -> (int) u.promptTokens()).orElse(0);
            int completionTokens = completion.usage()
                    .map(u -> (int) u.completionTokens()).orElse(0);

            TokenUsage usage = TokenUsage.of(promptTokens, completionTokens);
            EstimatedCost cost = pricingCatalog.estimateCost(model, promptTokens, completionTokens);
            long latency = System.currentTimeMillis() - start;

            return new ChatResponse(content, model, PROVIDER_ID, usage, cost, latency);

        } catch (Exception e) {
            throw new ProviderUnavailableException(PROVIDER_ID, e.getMessage(), e);
        }
    }

    private List<ChatCompletionMessageParam> toOpenAIMessages(List<ChatMessage> messages) {
        return messages.stream()
                .map(msg -> switch (msg.role()) {
                    case "system" -> ChatCompletionMessageParam.ofSystem(
                            com.openai.models.chat.completions.ChatCompletionSystemMessageParam.builder()
                                    .content(msg.content()).build());
                    case "assistant" -> ChatCompletionMessageParam.ofAssistant(
                            com.openai.models.chat.completions.ChatCompletionAssistantMessageParam.builder()
                                    .content(msg.content()).build());
                    default -> ChatCompletionMessageParam.ofUser(
                            com.openai.models.chat.completions.ChatCompletionUserMessageParam.builder()
                                    .content(msg.content()).build());
                })
                .toList();
    }
}
