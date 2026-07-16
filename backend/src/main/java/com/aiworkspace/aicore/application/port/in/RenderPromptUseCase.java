package com.aiworkspace.aicore.application.port.in;

import java.util.Map;

public interface RenderPromptUseCase {

    String execute(String templateName, Map<String, Object> variables);

    String execute(String templateName, int version, Map<String, Object> variables);
}
