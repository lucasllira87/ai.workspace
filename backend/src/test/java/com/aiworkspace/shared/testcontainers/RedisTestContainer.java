package com.aiworkspace.shared.testcontainers;

import org.testcontainers.containers.GenericContainer;

public final class RedisTestContainer {

    @SuppressWarnings({"resource", "rawtypes"})
    public static final GenericContainer<?> INSTANCE =
            new GenericContainer("redis:7-alpine")
                    .withExposedPorts(6379)
                    .withReuse(true);

    static {
        INSTANCE.start();
    }

    private RedisTestContainer() {}
}
