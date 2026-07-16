package com.aiworkspace.notifications.application.service;

import com.aiworkspace.notifications.application.command.MarkAsReadCommand;
import com.aiworkspace.notifications.application.command.SendNotificationCommand;
import com.aiworkspace.notifications.application.dto.NotificationDto;
import com.aiworkspace.notifications.application.port.in.GetNotificationsUseCase;
import com.aiworkspace.notifications.application.port.in.MarkAsReadUseCase;
import com.aiworkspace.notifications.application.port.in.SendNotificationUseCase;
import com.aiworkspace.notifications.application.port.out.EmailSenderPort;
import com.aiworkspace.notifications.application.port.out.NotificationPreferenceRepository;
import com.aiworkspace.notifications.application.port.out.NotificationRepository;
import com.aiworkspace.notifications.application.port.out.NotificationUserContextPort;
import com.aiworkspace.notifications.application.port.out.TemplateRendererPort;
import com.aiworkspace.notifications.domain.exception.NotificationNotFoundException;
import com.aiworkspace.notifications.domain.model.Notification;
import com.aiworkspace.notifications.domain.model.NotificationChannel;
import com.aiworkspace.notifications.domain.model.NotificationPreference;
import com.aiworkspace.notifications.domain.model.RenderedNotification;
import com.aiworkspace.shared.exception.DomainException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService implements SendNotificationUseCase, GetNotificationsUseCase, MarkAsReadUseCase {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final TemplateRendererPort templateRenderer;
    private final EmailSenderPort emailSender;
    private final NotificationUserContextPort userContext;
    private final MeterRegistry meterRegistry;

    private final Timer templateRenderTimer;
    private final Timer emailDeliveryTimer;

    public NotificationService(NotificationRepository notificationRepository,
                                NotificationPreferenceRepository preferenceRepository,
                                TemplateRendererPort templateRenderer,
                                EmailSenderPort emailSender,
                                NotificationUserContextPort userContext,
                                MeterRegistry meterRegistry) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.templateRenderer = templateRenderer;
        this.emailSender = emailSender;
        this.userContext = userContext;
        this.meterRegistry = meterRegistry;
        this.templateRenderTimer = Timer.builder("notifications.template.render")
                .description("Time to render a Thymeleaf notification template")
                .register(meterRegistry);
        this.emailDeliveryTimer = Timer.builder("notifications.email.delivery")
                .description("Time to deliver an email via SMTP")
                .register(meterRegistry);
    }

    @Override
    public void send(SendNotificationCommand command) {
        Optional<NotificationPreference> prefOpt = preferenceRepository.findByUserId(command.userId());
        if (prefOpt.isPresent() && !prefOpt.get().isEnabled(command.type(), command.channel())) {
            log.debug("Notification suppressed: userId={} type={} channel={}",
                    command.userId(), command.type(), command.channel());
            counter("notifications.skipped", command).increment();
            return;
        }

        RenderedNotification rendered = templateRenderTimer.record(
                () -> templateRenderer.render(command.type(), command.variables()));

        Notification notification = Notification.create(UUID.randomUUID(), command.userId(),
                command.type(), command.channel(), rendered);

        try {
            if (command.channel() == NotificationChannel.EMAIL) {
                String email = userContext.emailForUser(command.userId());
                emailDeliveryTimer.record(() -> emailSender.send(email, rendered));
            }
            notification.markSent();
            counter("notifications.sent", command).increment();
        } catch (Exception e) {
            log.error("Failed to send notification: userId={} type={} channel={} reason={}",
                    command.userId(), command.type(), command.channel(), e.getMessage());
            notification.markFailed(e.getMessage());
            counter("notifications.failed", command).increment();
        }

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getForUser(UUID userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(NotificationDto::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadForUser(UUID userId) {
        return notificationRepository.findUnreadInAppByUserId(userId)
                .stream().map(NotificationDto::from).collect(Collectors.toList());
    }

    @Override
    public void markAsRead(MarkAsReadCommand command) {
        Notification notification = notificationRepository.findById(command.notificationId())
                .orElseThrow(() -> new NotificationNotFoundException(command.notificationId()));

        if (!notification.getUserId().equals(command.userId())) {
            throw new DomainException("Access denied");
        }

        notification.markRead();
        notificationRepository.save(notification);
        meterRegistry.counter("notifications.read",
                "type", notification.getType().name()).increment();
    }

    private Counter counter(String name, SendNotificationCommand command) {
        return Counter.builder(name)
                .tag("type", command.type().name())
                .tag("channel", command.channel().name())
                .register(meterRegistry);
    }
}
