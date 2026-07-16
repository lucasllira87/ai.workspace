package com.aiworkspace.notifications.infrastructure.sse;

import com.aiworkspace.notifications.domain.event.NotificationSentEvent;
import com.aiworkspace.notifications.domain.model.NotificationChannel;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class NotificationSseService {

    private static final Logger log = LoggerFactory.getLogger(NotificationSseService.class);
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    public NotificationSseService(MeterRegistry meterRegistry) {
        Gauge.builder("notifications.sse.connections.active", activeConnections, AtomicInteger::get)
                .description("Number of active SSE connections")
                .register(meterRegistry);
    }

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        activeConnections.incrementAndGet();

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        return emitter;
    }

    @EventListener
    public void onNotificationSent(NotificationSentEvent event) {
        if (event.channel() != NotificationChannel.IN_APP) return;

        List<SseEmitter> userEmitters = emitters.get(event.userId());
        if (userEmitters == null || userEmitters.isEmpty()) return;

        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(Map.of(
                                "notificationId", event.notificationId().toString(),
                                "type", event.type().name(),
                                "occurredAt", event.occurredAt().toString()
                        )));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        if (!dead.isEmpty()) {
            userEmitters.removeAll(dead);
            activeConnections.addAndGet(-dead.size());
        }
    }

    // Keeps proxy-idle connections alive. Most reverse proxies (nginx, ALB) close
    // connections silent after 60–90 s of inactivity; 25 s is safely under that threshold.
    @Scheduled(fixedDelay = 25_000)
    public void sendHeartbeat() {
        List<SseEmitter> dead = new ArrayList<>();
        emitters.forEach((userId, userEmitters) -> {
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } catch (IOException e) {
                    dead.add(emitter);
                }
            }
            if (!dead.isEmpty()) {
                userEmitters.removeAll(dead);
                activeConnections.addAndGet(-dead.size());
                dead.clear();
            }
        });
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null && userEmitters.remove(emitter)) {
            activeConnections.decrementAndGet();
        }
    }
}
