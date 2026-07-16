package com.aiworkspace.shared.testcontainers;

import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresTestContainer {

    // Singleton container — started once per JVM, shared across all test classes.
    // pgvector image required because V010 migration installs the vector extension.
    @SuppressWarnings("resource")
    public static final PostgreSQLContainer<?> INSTANCE =
            new PostgreSQLContainer<>("pgvector/pgvector:pg16")
                    .withDatabaseName("aiworkspace_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    static {
        INSTANCE.start();
    }

    private PostgresTestContainer() {}
}
