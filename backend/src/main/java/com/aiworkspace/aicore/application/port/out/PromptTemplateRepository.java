package com.aiworkspace.aicore.application.port.out;

import com.aiworkspace.aicore.domain.model.PromptTemplate;

import java.util.List;
import java.util.Optional;

public interface PromptTemplateRepository {

    Optional<PromptTemplate> findActiveByName(String name);

    Optional<PromptTemplate> findByNameAndVersion(String name, int version);

    PromptTemplate save(PromptTemplate template);

    List<PromptTemplate> findByDomain(String domain);
}
