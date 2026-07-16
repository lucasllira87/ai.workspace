package com.aiworkspace.aicore.application.service;

import com.aiworkspace.aicore.application.command.ChatCommand;
import com.aiworkspace.aicore.application.dto.ChatMessage;
import com.aiworkspace.aicore.application.dto.ChatRequest;
import com.aiworkspace.aicore.application.dto.ChatResponse;
import com.aiworkspace.aicore.application.orchestrator.AIOrchestrator;
import com.aiworkspace.aicore.application.port.in.ChatUseCase;
import com.aiworkspace.aicore.application.port.out.AIConfigPort;
import com.aiworkspace.aicore.application.prompt.PromptEngine;
import com.aiworkspace.aicore.domain.event.AIRequestCompletedEvent;
import com.aiworkspace.aicore.domain.model.ModelId;
import com.aiworkspace.aicore.domain.model.TokenUsage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ChatService implements ChatUseCase {

    private final AIOrchestrator orchestrator;
    private final PromptEngine promptEngine;
    private final AIConfigPort aiConfig;
    private final ApplicationEventPublisher eventPublisher;

    public ChatService(AIOrchestrator orchestrator,
                        PromptEngine promptEngine,
                        AIConfigPort aiConfig,
                        ApplicationEventPublisher eventPublisher) {
        this.orchestrator = orchestrator;
        this.promptEngine = promptEngine;
        this.aiConfig = aiConfig;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ChatResponse execute(ChatCommand command) {
        List<ChatMessage> messages = resolveMessages(command);
        ModelId model = command.preferredModel() != null
                ? command.preferredModel()
                : ModelId.of(aiConfig.getDefaultChatModel());

        ChatRequest request = new ChatRequest(model, messages, null, null,
                Map.of("module", command.module() != null ? command.module() : "unknown",
                       "requestedBy", command.requestedBy() != null ? command.requestedBy() : ""));

        ChatResponse response = orchestrator.chat(request);

        eventPublisher.publishEvent(new AIRequestCompletedEvent(
                response.provider(), response.model(),
                command.module() != null ? command.module() : "unknown",
                command.requestedBy() != null ? command.requestedBy() : "",
                "chat",
                response.usage() != null ? response.usage() : TokenUsage.zero(),
                response.estimatedCost(), response.latencyMs(), true, Instant.now()));

        return response;
    }

    private List<ChatMessage> resolveMessages(ChatCommand command) {
        if (command.templateName() != null) {
            String rendered = promptEngine.render(command.templateName(),
                    command.templateVariables());
            return List.of(ChatMessage.user(rendered));
        }
        return command.messages();
    }
}
