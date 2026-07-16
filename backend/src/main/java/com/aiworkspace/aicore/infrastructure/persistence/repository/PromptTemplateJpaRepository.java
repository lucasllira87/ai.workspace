package com.aiworkspace.aicore.infrastructure.persistence.repository;

import com.aiworkspace.aicore.infrastructure.persistence.entity.PromptTemplateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromptTemplateJpaRepository
        extends JpaRepository<PromptTemplateJpaEntity, UUID> {

    Optional<PromptTemplateJpaEntity> findByNameAndActiveTrue(String name);

    Optional<PromptTemplateJpaEntity> findByNameAndVersion(String name, int version);

    List<PromptTemplateJpaEntity> findByDomain(String domain);
}
