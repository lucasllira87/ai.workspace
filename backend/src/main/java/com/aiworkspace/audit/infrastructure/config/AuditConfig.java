package com.aiworkspace.audit.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
@EnableScheduling
public class AuditConfig {

    // Qualifier "auditAsyncExecutor" avoids collision with other modules' executor beans.
    // Virtual threads (Java 21) are ideal for parallel blocking I/O in DashboardService.
    @Bean(name = "auditAsyncExecutor")
    public Executor auditAsyncExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
