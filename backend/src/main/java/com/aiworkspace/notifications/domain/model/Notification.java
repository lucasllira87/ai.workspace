package com.aiworkspace.notifications.domain.model;

import com.aiworkspace.notifications.domain.event.NotificationFailedEvent;
import com.aiworkspace.notifications.domain.event.NotificationSentEvent;
import com.aiworkspace.shared.domain.AggregateRoot;
import com.aiworkspace.shared.exception.DomainException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Notification extends AggregateRoot {

    private final UUID id;
    private final UUID userId;
    private final NotificationType type;
    private final NotificationChannel channel;
    private final String subject;
    private final String body;
    private NotificationStatus status;
    private String failedReason;
    private Instant sentAt;
    private Instant readAt;
    private final Instant createdAt;

    private Notification(UUID id, UUID userId, NotificationType type, NotificationChannel channel,
                          String subject, String body, NotificationStatus status,
                          String failedReason, Instant sentAt, Instant readAt, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.channel = channel;
        this.subject = subject;
        this.body = body;
        this.status = status;
        this.failedReason = failedReason;
        this.sentAt = sentAt;
        this.readAt = readAt;
        this.createdAt = createdAt;
    }

    public static Notification create(UUID id, UUID userId, NotificationType type,
                                       NotificationChannel channel, RenderedNotification rendered) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(rendered, "rendered");

        return new Notification(id, userId, type, channel,
                rendered.subject(), rendered.bodyHtml(),
                NotificationStatus.PENDING, null, null, null, Instant.now());
    }

    public static Notification reconstitute(UUID id, UUID userId, NotificationType type,
                                             NotificationChannel channel, String subject, String body,
                                             NotificationStatus status, String failedReason,
                                             Instant sentAt, Instant readAt, Instant createdAt) {
        return new Notification(id, userId, type, channel, subject, body,
                status, failedReason, sentAt, readAt, createdAt);
    }

    public void markSent() {
        if (status == NotificationStatus.SENT) return;
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
        registerEvent(new NotificationSentEvent(id, userId, type, channel, sentAt));
    }

    public void markFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failedReason = reason;
        registerEvent(new NotificationFailedEvent(id, userId, type, channel, reason, Instant.now()));
    }

    public void markRead() {
        if (status == NotificationStatus.READ) return;
        if (channel != NotificationChannel.IN_APP) {
            throw new DomainException("Only IN_APP notifications can be marked as read");
        }
        this.status = NotificationStatus.READ;
        this.readAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public NotificationType getType() { return type; }
    public NotificationChannel getChannel() { return channel; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public NotificationStatus getStatus() { return status; }
    public String getFailedReason() { return failedReason; }
    public Instant getSentAt() { return sentAt; }
    public Instant getReadAt() { return readAt; }
    public Instant getCreatedAt() { return createdAt; }
}
