package com.aiworkspace.aicore.application.prompt;

import com.aiworkspace.aicore.application.port.out.PromptTemplateRepository;
import com.aiworkspace.aicore.domain.exception.PromptTemplateNotFoundException;
import com.aiworkspace.aicore.domain.model.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DefaultPromptEngine implements PromptEngine, com.aiworkspace.aicore.application.port.in.RenderPromptUseCase {

    private final PromptTemplateRepository repository;

    public DefaultPromptEngine(PromptTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public String render(String templateName, Map<String, Object> variables) {
        PromptTemplate template = repository.findActiveByName(templateName)
                .orElseThrow(() -> new PromptTemplateNotFoundException(templateName));
        return template.render(variables);
    }

    @Override
    public String render(String templateName, int version, Map<String, Object> variables) {
        PromptTemplate template = repository.findByNameAndVersion(templateName, version)
                .orElseThrow(() -> new PromptTemplateNotFoundException(templateName, version));
        return template.render(variables);
    }

    @Override
    public String execute(String templateName, Map<String, Object> variables) {
        return render(templateName, variables);
    }

    @Override
    public String execute(String templateName, int version, Map<String, Object> variables) {
        return render(templateName, version, variables);
    }
}
