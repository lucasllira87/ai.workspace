package com.aiworkspace.aicore.infrastructure.persistence.adapter;

import com.aiworkspace.aicore.application.port.out.PromptTemplateRepository;
import com.aiworkspace.aicore.domain.model.PromptTemplate;
import com.aiworkspace.aicore.infrastructure.persistence.entity.PromptTemplateJpaEntity;
import com.aiworkspace.aicore.infrastructure.persistence.repository.PromptTemplateJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class PromptTemplateRepositoryAdapter implements PromptTemplateRepository {

    private final PromptTemplateJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public PromptTemplateRepositoryAdapter(PromptTemplateJpaRepository jpaRepository,
                                            ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<PromptTemplate> findActiveByName(String name) {
        return jpaRepository.findByNameAndActiveTrue(name).map(this::toDomain);
    }

    @Override
    public Optional<PromptTemplate> findByNameAndVersion(String name, int version) {
        return jpaRepository.findByNameAndVersion(name, version).map(this::toDomain);
    }

    @Override
    public PromptTemplate save(PromptTemplate template) {
        PromptTemplateJpaEntity entity = toEntity(template);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<PromptTemplate> findByDomain(String domain) {
        return jpaRepository.findByDomain(domain).stream().map(this::toDomain).toList();
    }

    private PromptTemplate toDomain(PromptTemplateJpaEntity entity) {
        List<String> variables = deserializeVariables(entity.getVariables());
        return PromptTemplate.reconstitute(
                entity.getId(), entity.getName(), entity.getDomain(),
                entity.getVersion(), entity.getContent(), variables,
                entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private PromptTemplateJpaEntity toEntity(PromptTemplate template) {
        PromptTemplateJpaEntity entity = new PromptTemplateJpaEntity();
        entity.setId(template.getId());
        entity.setName(template.getName());
        entity.setDomain(template.getDomain());
        entity.setVersion(template.getVersion());
        entity.setContent(template.getContent());
        entity.setVariables(serializeVariables(template.getVariables()));
        entity.setActive(template.isActive());
        entity.setCreatedAt(template.getCreatedAt());
        entity.setUpdatedAt(template.getUpdatedAt());
        return entity;
    }

    private String serializeVariables(List<String> variables) {
        if (variables == null || variables.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> deserializeVariables(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
