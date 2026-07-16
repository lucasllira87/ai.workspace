package com.aiworkspace.aicore.application.command;

import com.aiworkspace.aicore.application.dto.ChatMessage;
import com.aiworkspace.aicore.domain.model.ModelId;

import java.util.List;
import java.util.Map;

public record ChatCommand(
        String templateName,
        Map<String, Object> templateVariables,
        List<ChatMessage> messages,
        ModelId preferredModel,
        String module,
        String requestedBy
) {
    public static ChatCommand fromTemplate(String templateName, Map<String, Object> variables,
                                            String module, String requestedBy) {
        return new ChatCommand(templateName, variables, null, null, module, requestedBy);
    }

    public static ChatCommand fromMessages(List<ChatMessage> messages, ModelId model,
                                            String module, String requestedBy) {
        return new ChatCommand(null, null, messages, model, module, requestedBy);
    }
}
