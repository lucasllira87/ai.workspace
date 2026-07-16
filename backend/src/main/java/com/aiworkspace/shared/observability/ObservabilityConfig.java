package com.aiworkspace.shared.observability;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ObservabilityConfig implements WebMvcConfigurer {

    private final MdcUserInterceptor mdcUserInterceptor;

    public ObservabilityConfig(MdcUserInterceptor mdcUserInterceptor) {
        this.mdcUserInterceptor = mdcUserInterceptor;
    }

    /**
     * Applies common tags to every metric: application name and active Spring profile.
     * Uses Environment.getActiveProfiles() — reliable across all contexts including tests.
     * @Value("${spring.profiles.active}") is a Bootstrap property and not always resolvable.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags(
            @Value("${spring.application.name}") String appName,
            Environment environment) {
        String profile = environment.getActiveProfiles().length > 0
                ? String.join(",", environment.getActiveProfiles())
                : "default";
        return registry -> registry.config()
                .commonTags("application", appName, "environment", profile);
    }

    /**
     * Enables @Timed annotation on any Spring bean method.
     * Spring Boot 3.2+ auto-configures this when spring-boot-starter-aop is present,
     * but declaring it explicitly makes the dependency clear.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mdcUserInterceptor);
    }
}
