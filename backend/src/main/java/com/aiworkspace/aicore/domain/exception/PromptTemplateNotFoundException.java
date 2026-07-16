package com.aiworkspace.aicore.domain.exception;

import com.aiworkspace.shared.exception.NotFoundException;

public class PromptTemplateNotFoundException extends NotFoundException {

    public PromptTemplateNotFoundException(String name) {
        super("Prompt template not found: " + name);
    }

    public PromptTemplateNotFoundException(String name, int version) {
        super("Prompt template not found: " + name + " v" + version);
    }
}
