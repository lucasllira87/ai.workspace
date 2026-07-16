package com.aiworkspace.aicore.domain.model;

import com.aiworkspace.aicore.domain.exception.MissingTemplateVariableException;
import com.aiworkspace.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PromptTemplate extends AggregateRoot {

    private final UUID id;
    private final String name;
    private final String domain;
    private final int version;
    private final String content;
    private final List<String> variables;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;

    private PromptTemplate(UUID id, String name, String domain, int version,
                            String content, List<String> variables, boolean active,
                            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.version = version;
        this.content = content;
        this.variables = List.copyOf(variables);
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PromptTemplate create(String name, String domain, String content,
                                         List<String> variables) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(domain, "domain");
        Objects.requireNonNull(content, "content");
        Instant now = Instant.now();
        return new PromptTemplate(UUID.randomUUID(), name, domain, 1,
                content, variables != null ? variables : List.of(), true, now, now);
    }

    public static PromptTemplate reconstitute(UUID id, String name, String domain, int version,
                                               String content, List<String> variables,
                                               boolean active, Instant createdAt, Instant updatedAt) {
        return new PromptTemplate(id, name, domain, version, content,
                variables != null ? variables : List.of(), active, createdAt, updatedAt);
    }

    public String render(Map<String, Object> vars) {
        Set<String> provided = vars != null ? vars.keySet() : Set.of();
        Set<String> missing = variables.stream()
                .filter(v -> !provided.contains(v))
                .collect(Collectors.toSet());

        if (!missing.isEmpty()) {
            throw new MissingTemplateVariableException(name, missing);
        }

        String result = content;
        if (vars != null) {
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
            }
        }
        return result;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDomain() { return domain; }
    public int getVersion() { return version; }
    public String getContent() { return content; }
    public List<String> getVariables() { return variables; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
