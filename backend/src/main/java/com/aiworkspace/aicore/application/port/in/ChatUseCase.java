package com.aiworkspace.aicore.application.port.in;

import com.aiworkspace.aicore.application.command.ChatCommand;
import com.aiworkspace.aicore.application.dto.ChatResponse;

public interface ChatUseCase {

    ChatResponse execute(ChatCommand command);
}
