package com.aiworkspace.aicore.infrastructure.provider.anthropic;

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
import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.TextBlockParam;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AnthropicChatAdapter implements ChatProvider {

    private static final ProviderId PROVIDER_ID = ProviderId.ANTHROPIC;

    private static final Set<ModelId> SUPPORTED_MODELS = Set.of(
            ModelId.CLAUDE_OPUS_4,
            ModelId.CLAUDE_SONNET_4,
            ModelId.CLAUDE_HAIKU_4
    );

    private static final int DEFAULT_MAX_TOKENS = 2048;

    private final AnthropicClient client;
    private final ModelPricingCatalog pricingCatalog;

    public AnthropicChatAdapter(AnthropicClient client, ModelPricingCatalog pricingCatalog) {
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
            ModelId model = request.model() != null ? request.model() : ModelId.CLAUDE_SONNET_4;

            // Extract system prompt if present
            String systemPrompt = request.messages().stream()
                    .filter(m -> "system".equals(m.role()))
                    .map(ChatMessage::content)
                    .collect(Collectors.joining("\n"));

            List<MessageParam> userMessages = request.messages().stream()
                    .filter(m -> !"system".equals(m.role()))
                    .map(m -> MessageParam.builder()
                            .role("user".equals(m.role())
                                    ? MessageParam.Role.USER
                                    : MessageParam.Role.ASSISTANT)
                            .content(MessageParam.Content.ofText(
                                    TextBlockParam.builder().text(m.content()).build()))
                            .build())
                    .toList();

            MessageCreateParams.Builder builder = MessageCreateParams.builder()
                    .model(Model.of(model.value()))
                    .maxTokens(request.maxTokens() != null ? request.maxTokens() : DEFAULT_MAX_TOKENS)
                    .messages(userMessages);

            if (!systemPrompt.isBlank()) {
                builder.system(systemPrompt);
            }

            Message message = client.messages().create(builder.build());

            String content = message.content().stream()
                    .filter(b -> b.isText())
                    .map(b -> b.asText().text())
                    .collect(Collectors.joining());

            int promptTokens = message.usage() != null ? (int) message.usage().inputTokens() : 0;
            int completionTokens = message.usage() != null ? (int) message.usage().outputTokens() : 0;

            TokenUsage usage = TokenUsage.of(promptTokens, completionTokens);
            EstimatedCost cost = pricingCatalog.estimateCost(model, promptTokens, completionTokens);
            long latency = System.currentTimeMillis() - start;

            return new ChatResponse(content, model, PROVIDER_ID, usage, cost, latency);

        } catch (Exception e) {
            throw new ProviderUnavailableException(PROVIDER_ID, e.getMessage(), e);
        }
    }
}
