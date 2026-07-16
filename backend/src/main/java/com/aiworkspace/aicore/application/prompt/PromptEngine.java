package com.aiworkspace.aicore.application.prompt;

import java.util.Map;

public interface PromptEngine {

    String render(String templateName, Map<String, Object> variables);

    String render(String templateName, int version, Map<String, Object> variables);
}
