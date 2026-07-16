package com.aiworkspace.notifications.presentation;

import com.aiworkspace.notifications.application.command.MarkAsReadCommand;
import com.aiworkspace.notifications.application.dto.NotificationDto;
import com.aiworkspace.notifications.application.port.in.GetNotificationsUseCase;
import com.aiworkspace.notifications.application.port.in.MarkAsReadUseCase;
import com.aiworkspace.notifications.application.port.out.NotificationUserContextPort;
import com.aiworkspace.notifications.infrastructure.sse.NotificationSseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final GetNotificationsUseCase getNotifications;
    private final MarkAsReadUseCase markAsRead;
    private final NotificationUserContextPort userContext;
    private final NotificationSseService sseService;

    public NotificationController(GetNotificationsUseCase getNotifications,
                                   MarkAsReadUseCase markAsRead,
                                   NotificationUserContextPort userContext,
                                   NotificationSseService sseService) {
        this.getNotifications = getNotifications;
        this.markAsRead = markAsRead;
        this.userContext = userContext;
        this.sseService = sseService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> list() {
        UUID userId = userContext.currentUserId();
        return ResponseEntity.ok(getNotifications.getForUser(userId));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> listUnread() {
        UUID userId = userContext.currentUserId();
        return ResponseEntity.ok(getNotifications.getUnreadForUser(userId));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        UUID userId = userContext.currentUserId();
        return sseService.subscribe(userId);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable UUID id) {
        UUID userId = userContext.currentUserId();
        markAsRead.markAsRead(new MarkAsReadCommand(id, userId));
        return ResponseEntity.noContent().build();
    }
}
