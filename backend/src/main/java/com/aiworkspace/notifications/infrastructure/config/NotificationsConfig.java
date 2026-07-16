package com.aiworkspace.notifications.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAsync
public class NotificationsConfig {
    // @EnableScheduling activates the SSE heartbeat @Scheduled task
    // @EnableAsync enables @Async on event listener methods so that notification
    // dispatch does not block the billing/learning/documents transaction threads
}
